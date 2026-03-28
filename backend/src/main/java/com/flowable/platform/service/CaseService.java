package com.flowable.platform.service;

import com.flowable.platform.config.MultiTenantFilter;
import com.flowable.platform.dto.*;
import com.flowable.platform.util.CmmnDiGenerator;
import org.flowable.engine.TaskService;
import org.flowable.cmmn.api.CmmnHistoryService;
import org.flowable.cmmn.api.CmmnRepositoryService;
import org.flowable.cmmn.api.CmmnRuntimeService;
import org.flowable.cmmn.converter.CmmnXmlConverter;
import org.flowable.cmmn.model.CmmnModel;
import org.flowable.cmmn.api.history.HistoricCaseInstance;
import org.flowable.cmmn.api.history.HistoricCaseInstanceQuery;
import org.flowable.cmmn.api.repository.CaseDefinition;
import org.flowable.cmmn.api.repository.CmmnDeployment;
import org.flowable.cmmn.api.runtime.CaseInstance;
import org.flowable.cmmn.api.runtime.CaseInstanceQuery;
import org.flowable.cmmn.api.runtime.PlanItemInstance;
import org.flowable.engine.IdentityService;
import org.flowable.task.api.TaskInfo;
import org.flowable.task.api.Task;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CaseService {

    private final CmmnRuntimeService cmmnRuntimeService;
    private final CmmnHistoryService cmmnHistoryService;
    private final CmmnRepositoryService cmmnRepositoryService;
    private final AuditService auditService;
    private final IdentityService identityService;
    private final TaskService taskService;

    public CaseService(CmmnRuntimeService cmmnRuntimeService,
                       CmmnHistoryService cmmnHistoryService,
                       CmmnRepositoryService cmmnRepositoryService,
                       AuditService auditService,
                       IdentityService identityService,
                       TaskService taskService) {
        this.cmmnRuntimeService = cmmnRuntimeService;
        this.cmmnHistoryService = cmmnHistoryService;
        this.cmmnRepositoryService = cmmnRepositoryService;
        this.auditService = auditService;
        this.identityService = identityService;
        this.taskService = taskService;
    }

    public List<DefinitionDTO> listCaseDefinitions() {
        String tenantId = MultiTenantFilter.getCurrentTenantId();

        // Get both tenant-specific AND global (null tenant) case definitions
        // Global definitions are auto-deployed from classpath and available to all tenants
        List<CaseDefinition> allDefinitions = new ArrayList<>();

        // Add tenant-specific definitions
        if (tenantId != null) {
            List<CaseDefinition> tenantDefinitions = cmmnRepositoryService
                    .createCaseDefinitionQuery()
                    .latestVersion()
                    .caseDefinitionTenantId(tenantId)
                    .list();
            allDefinitions.addAll(tenantDefinitions);
        }

        // Add global (auto-deployed) definitions
        List<CaseDefinition> globalDefinitions = cmmnRepositoryService
                .createCaseDefinitionQuery()
                .latestVersion()
                .caseDefinitionTenantId("")  // Empty string = global definitions
                .list();

        // Merge, avoiding duplicates by key
        for (CaseDefinition globalDef : globalDefinitions) {
            boolean exists = allDefinitions.stream()
                    .anyMatch(def -> def.getKey().equals(globalDef.getKey()));
            if (!exists) {
                allDefinitions.add(globalDef);
            }
        }

        return allDefinitions.stream()
                .map(this::toDefinitionDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public InstanceDTO startCaseInstance(String caseDefinitionKey, Map<String, Object> variables, String businessKey) {
        String tenantId = MultiTenantFilter.getCurrentTenantId();

        // Find case definition (check tenant-specific first, then global)
        CaseDefinition definition = null;

        if (tenantId != null) {
            // Try tenant-specific first
            definition = cmmnRepositoryService.createCaseDefinitionQuery()
                    .caseDefinitionKey(caseDefinitionKey)
                    .caseDefinitionTenantId(tenantId)
                    .latestVersion()
                    .singleResult();
        }

        // Fall back to global (empty tenant) if not found
        if (definition == null) {
            definition = cmmnRepositoryService.createCaseDefinitionQuery()
                    .caseDefinitionKey(caseDefinitionKey)
                    .caseDefinitionTenantId("")
                    .latestVersion()
                    .singleResult();
        }

        if (definition == null) {
            throw new IllegalArgumentException("Case definition not found: " + caseDefinitionKey);
        }

        // Set authenticated user context for Flowable's ${initiator} variable
        String authenticatedUserId = getCurrentUser();
        System.out.println("=== DEBUG CaseService.startCaseInstance ===");
        System.out.println("Case definition key: " + caseDefinitionKey);
        System.out.println("Authenticated user from SecurityContext: " + authenticatedUserId);

        // Prepare variables, adding initiator if authenticated
        Map<String, Object> variablesWithInitiator = new HashMap<>();
        if (variables != null) {
            variablesWithInitiator.putAll(variables);
        }
        if (authenticatedUserId != null) {
            variablesWithInitiator.put("initiator", authenticatedUserId);
            System.out.println("Added initiator variable: " + authenticatedUserId);
        }

        try {
            if (authenticatedUserId != null) {
                identityService.setAuthenticatedUserId(authenticatedUserId);
                System.out.println("Set authenticatedUserId in Flowable: " + authenticatedUserId);
            } else {
                System.out.println("WARNING: authenticatedUserId is NULL - ${initiator} will not work!");
            }

            // Start case instance with appropriate tenant context
            CaseInstance caseInstance;
            if (tenantId != null && !"".equals(definition.getTenantId())) {
                // Tenant-specific case
                caseInstance = cmmnRuntimeService.createCaseInstanceBuilder()
                        .caseDefinitionId(definition.getId())
                        .variables(variablesWithInitiator)
                        .businessKey(businessKey)
                        .tenantId(tenantId)
                        .start();
            } else {
                // Global case - use the case definition's tenant (empty)
                caseInstance = cmmnRuntimeService.createCaseInstanceBuilder()
                        .caseDefinitionId(definition.getId())
                        .variables(variablesWithInitiator)
                        .businessKey(businessKey)
                        .start();
            }

            auditService.logAction("CASE_STARTED", "CASE_INSTANCE", caseInstance.getId(), null);

            return toInstanceDTO(caseInstance, definition);
        } finally {
            // Always clear the authenticated user to avoid thread pool contamination
            identityService.setAuthenticatedUserId(null);
        }
    }

    public List<InstanceDTO> listActiveCaseInstances(int page, int size, String search, String startedBy,
                                                     LocalDateTime startDateFrom, LocalDateTime startDateTo) {
        String tenantId = MultiTenantFilter.getCurrentTenantId();

        CaseInstanceQuery query = cmmnRuntimeService.createCaseInstanceQuery();
        if (tenantId != null) {
            // Include both tenant-specific instances AND global instances (no tenant ID)
            query.or()
                   .caseInstanceTenantId(tenantId)
                   .caseInstanceTenantId("")  // Empty string = global instances
               .endOr();
        }
        if (startedBy != null && !startedBy.isBlank()) {
            query.caseInstanceStartedBy(startedBy);
        }
        if (startDateFrom != null) {
            query.caseInstanceStartedAfter(Date.from(startDateFrom.atZone(ZoneId.systemDefault()).toInstant()));
        }
        if (startDateTo != null) {
            query.caseInstanceStartedBefore(Date.from(startDateTo.atZone(ZoneId.systemDefault()).toInstant()));
        }

        query.orderByStartTime().desc();
        List<CaseInstance> instances = query.listPage(page * size, size);

        return instances.stream()
                .map(ci -> {
                    CaseDefinition def = cmmnRepositoryService.createCaseDefinitionQuery()
                            .caseDefinitionId(ci.getCaseDefinitionId())
                            .singleResult();
                    return toInstanceDTO(ci, def);
                })
                .collect(Collectors.toList());
    }

    public long countActiveCaseInstances(String startedBy) {
        String tenantId = MultiTenantFilter.getCurrentTenantId();
        CaseInstanceQuery query = cmmnRuntimeService.createCaseInstanceQuery();
        if (tenantId != null) {
            // Include both tenant-specific instances AND global instances (no tenant ID)
            query.or()
                   .caseInstanceTenantId(tenantId)
                   .caseInstanceTenantId("")  // Empty string = global instances
               .endOr();
        }
        if (startedBy != null && !startedBy.isBlank()) {
            query.caseInstanceStartedBy(startedBy);
        }
        return query.count();
    }

    public List<InstanceDTO> listCompletedCaseInstances(int page, int size, String search, String startedBy,
                                                        InstanceStatus statusFilter,
                                                        LocalDateTime startDateFrom, LocalDateTime startDateTo,
                                                        LocalDateTime endDateFrom, LocalDateTime endDateTo) {
        String tenantId = MultiTenantFilter.getCurrentTenantId();

        HistoricCaseInstanceQuery query = cmmnHistoryService.createHistoricCaseInstanceQuery()
                .finished();
        if (tenantId != null) {
            // Include both tenant-specific instances AND global instances (no tenant ID)
            query.or()
                   .caseInstanceTenantId(tenantId)
                   .caseInstanceTenantId("")  // Empty string = global instances
               .endOr();
        }
        if (startedBy != null && !startedBy.isBlank()) {
            query.startedBy(startedBy);
        }
        if (startDateFrom != null) {
            query.startedAfter(Date.from(startDateFrom.atZone(ZoneId.systemDefault()).toInstant()));
        }
        if (startDateTo != null) {
            query.startedBefore(Date.from(startDateTo.atZone(ZoneId.systemDefault()).toInstant()));
        }
        if (endDateFrom != null) {
            query.finishedAfter(Date.from(endDateFrom.atZone(ZoneId.systemDefault()).toInstant()));
        }
        if (endDateTo != null) {
            query.finishedBefore(Date.from(endDateTo.atZone(ZoneId.systemDefault()).toInstant()));
        }

        query.orderByEndTime().desc();
        List<HistoricCaseInstance> instances = query.listPage(page * size, size);

        return instances.stream()
                .map(this::toInstanceDTOFromHistory)
                .collect(Collectors.toList());
    }

    public long countCompletedCaseInstances() {
        String tenantId = MultiTenantFilter.getCurrentTenantId();
        HistoricCaseInstanceQuery query = cmmnHistoryService.createHistoricCaseInstanceQuery().finished();
        if (tenantId != null) {
            // Include both tenant-specific instances AND global instances (no tenant ID)
            query.or()
                   .caseInstanceTenantId(tenantId)
                   .caseInstanceTenantId("")  // Empty string = global instances
               .endOr();
        }
        return query.count();
    }

    public InstanceDetailDTO getCaseInstanceDetail(String caseInstanceId) {
        // Try active first
        CaseInstance active = cmmnRuntimeService.createCaseInstanceQuery()
                .caseInstanceId(caseInstanceId)
                .singleResult();

        if (active != null) {
            CaseDefinition def = cmmnRepositoryService.createCaseDefinitionQuery()
                    .caseDefinitionId(active.getCaseDefinitionId())
                    .singleResult();

            InstanceDetailDTO detail = new InstanceDetailDTO();
            copyInstanceFields(detail, toInstanceDTO(active, def));
            detail.setVariables(cmmnRuntimeService.getVariables(caseInstanceId));

            // Get active plan items
            List<PlanItemInstance> planItems = cmmnRuntimeService.createPlanItemInstanceQuery()
                    .caseInstanceId(caseInstanceId)
                    .planItemInstanceStateActive()
                    .list();
            detail.setCurrentActivities(planItems.stream()
                    .map(PlanItemInstance::getName)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList()));

            // Active tasks for this case instance
            List<Task> tasks = taskService.createTaskQuery()
                    .caseInstanceId(caseInstanceId)
                    .list();
            detail.setTasks(tasks.stream().map(this::toTaskDTO).collect(Collectors.toList()));

            return detail;
        }

        // Try historic
        HistoricCaseInstance historic = cmmnHistoryService.createHistoricCaseInstanceQuery()
                .caseInstanceId(caseInstanceId)
                .singleResult();

        if (historic == null) {
            throw new IllegalArgumentException("Case instance not found: " + caseInstanceId);
        }

        InstanceDetailDTO detail = new InstanceDetailDTO();
        copyInstanceFields(detail, toInstanceDTOFromHistory(historic));
        return detail;
    }

    private DefinitionDTO toDefinitionDTO(CaseDefinition def) {
        LocalDateTime deploymentTime = null;
        if (def.getDeploymentId() != null) {
            CmmnDeployment deployment = cmmnRepositoryService.createDeploymentQuery()
                    .deploymentId(def.getDeploymentId())
                    .singleResult();
            if (deployment != null && deployment.getDeploymentTime() != null) {
                deploymentTime = deployment.getDeploymentTime().toInstant()
                        .atZone(ZoneId.systemDefault()).toLocalDateTime();
            }
        }

        return new DefinitionDTO(
                def.getId(), def.getKey(), def.getName(), def.getVersion(),
                def.getCategory(), DefinitionType.CMMN, def.hasStartFormKey(), deploymentTime
        );
    }

    private InstanceDTO toInstanceDTO(CaseInstance ci, CaseDefinition def) {
        InstanceDTO dto = new InstanceDTO();
        dto.setId(ci.getId());
        dto.setDefinitionId(ci.getCaseDefinitionId());
        dto.setDefinitionKey(ci.getCaseDefinitionKey());
        dto.setDefinitionName(def != null ? def.getName() : ci.getCaseDefinitionKey());
        dto.setType(DefinitionType.CMMN);
        dto.setStartTime(ci.getStartTime() != null ?
                ci.getStartTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime() : null);
        dto.setStartedBy(ci.getStartUserId());
        dto.setStatus(InstanceStatus.ACTIVE);
        dto.setBusinessKey(ci.getBusinessKey());
        dto.setTenantId(ci.getTenantId());
        return dto;
    }

    private InstanceDTO toInstanceDTOFromHistory(HistoricCaseInstance hci) {
        InstanceDTO dto = new InstanceDTO();
        dto.setId(hci.getId());
        dto.setDefinitionId(hci.getCaseDefinitionId());
        dto.setDefinitionKey(hci.getCaseDefinitionKey());
        dto.setDefinitionName(hci.getCaseDefinitionName());
        dto.setType(DefinitionType.CMMN);
        dto.setStartTime(hci.getStartTime() != null ?
                hci.getStartTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime() : null);
        dto.setEndTime(hci.getEndTime() != null ?
                hci.getEndTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime() : null);
        if (hci.getStartTime() != null && hci.getEndTime() != null) {
            dto.setDuration(hci.getEndTime().getTime() - hci.getStartTime().getTime());
        }
        dto.setStartedBy(hci.getStartUserId());
        dto.setStatus(mapCaseState(hci.getState()));
        dto.setBusinessKey(hci.getBusinessKey());
        dto.setTenantId(hci.getTenantId());
        return dto;
    }

    private InstanceStatus mapCaseState(String state) {
        if (state == null) return InstanceStatus.COMPLETED;
        return switch (state.toLowerCase()) {
            case "active" -> InstanceStatus.ACTIVE;
            case "completed" -> InstanceStatus.COMPLETED;
            case "terminated" -> InstanceStatus.CANCELLED;
            default -> InstanceStatus.COMPLETED;
        };
    }

    private void copyInstanceFields(InstanceDetailDTO detail, InstanceDTO source) {
        detail.setId(source.getId());
        detail.setDefinitionId(source.getDefinitionId());
        detail.setDefinitionKey(source.getDefinitionKey());
        detail.setDefinitionName(source.getDefinitionName());
        detail.setType(source.getType());
        detail.setStartTime(source.getStartTime());
        detail.setEndTime(source.getEndTime());
        detail.setDuration(source.getDuration());
        detail.setStartedBy(source.getStartedBy());
        detail.setStatus(source.getStatus());
        detail.setBusinessKey(source.getBusinessKey());
        detail.setTenantId(source.getTenantId());
    }

    private TaskDTO toTaskDTO(Task task) {
        TaskDTO dto = new TaskDTO();
        dto.setId(task.getId());
        dto.setName(task.getName());
        dto.setAssignee(task.getAssignee());
        dto.setCreateTime(task.getCreateTime() != null ?
                task.getCreateTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime() : null);
        return dto;
    }

    private String getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            return authentication != null ? authentication.getName() : null;
        } catch (Exception e) {
            return null;
        }
    }

    public DiagramDataDTO getCaseInstanceDiagram(String caseInstanceId) {
        try {
            // Get case instance
            CaseInstance instance = cmmnRuntimeService.createCaseInstanceQuery()
                    .caseInstanceId(caseInstanceId)
                    .singleResult();

            if (instance == null) {
                return null;
            }

            // Get case definition
            CaseDefinition definition = cmmnRepositoryService.createCaseDefinitionQuery()
                    .caseDefinitionId(instance.getCaseDefinitionId())
                    .singleResult();

            if (definition == null) {
                return null;
            }

            // Get CMMN XML - ALWAYS use model conversion to get complete XML
            String diagramXml = null;

            // Get the CMMN model and convert to XML
            CmmnModel cmmnModel = cmmnRepositoryService.getCmmnModel(definition.getId());

            if (cmmnModel != null) {
                // Convert model to XML - this should include all elements
                CmmnXmlConverter xmlConverter = new CmmnXmlConverter();
                byte[] xmlBytes = xmlConverter.convertToXML(cmmnModel);
                diagramXml = new String(xmlBytes, StandardCharsets.UTF_8);

                // Generate CMMN DI if missing (required by cmmn-js for rendering)
                // Check for actual CMMNShape elements — the converter may produce an empty
                // <cmmndi:CMMNDI><cmmndi:CMMNDiagram/></cmmndi:CMMNDI> wrapper with no shapes
                boolean hasShapes = diagramXml.contains("<cmmndi:CMMNShape");

                if (diagramXml != null && !hasShapes) {
                    try {
                        diagramXml = CmmnDiGenerator.addDiInformation(diagramXml);
                    } catch (Exception e) {
                        // Continue with original XML (may result in empty diagram)
                    }
                }
            }

            // Fallback: try direct resource fetch if model is null
            if (diagramXml == null) {
                try {
                    InputStream cmmnResourceStream = cmmnRepositoryService.getResourceAsStream(
                            definition.getDeploymentId(),
                            definition.getResourceName()
                    );
                    if (cmmnResourceStream != null) {
                        diagramXml = new String(cmmnResourceStream.readAllBytes(),
                                StandardCharsets.UTF_8);
                    }
                } catch (Exception e) {
                    // Continue with model-converted XML
                }
            }

            // Calculate active and completed elements
            List<String> activeElementIds = new ArrayList<>();
            List<String> completedElementIds = new ArrayList<>();
            String currentElementId = null;

            // Get active plan items (human tasks, stages, milestones currently active)
            List<PlanItemInstance> activePlanItems = cmmnRuntimeService.createPlanItemInstanceQuery()
                    .caseInstanceId(caseInstanceId)
                    .planItemInstanceStateActive()
                    .list();

            for (PlanItemInstance planItem : activePlanItems) {
                String elementId = planItem.getElementId();
                String planItemId = planItem.getId();

                // Add both planItem ID and element ID to handle both cases
                if (planItemId != null) {
                    activeElementIds.add(planItemId);
                }
                if (elementId != null && !elementId.equals(planItemId)) {
                    activeElementIds.add(elementId);
                }

                // Use first active plan item as current element
                if (currentElementId == null) {
                    currentElementId = planItemId != null ? planItemId : elementId;
                }
            }

            // Note: Historic plan item queries are not available in all Flowable versions
            // For now, completed elements can be populated later if needed

            // Create diagram data DTO
            DiagramDataDTO diagramData = new DiagramDataDTO();
            diagramData.setDiagramXml(diagramXml);
            diagramData.setActiveElementIds(activeElementIds);
            diagramData.setCompletedElementIds(completedElementIds);
            diagramData.setCurrentElementId(currentElementId);

            return diagramData;
        } catch (Exception e) {
            throw new RuntimeException("Error writing CMMN XML: " + e.getMessage(), e);
        }
    }
}
