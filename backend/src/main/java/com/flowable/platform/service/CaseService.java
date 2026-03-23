package com.flowable.platform.service;

import com.flowable.platform.config.MultiTenantFilter;
import com.flowable.platform.dto.*;
import org.flowable.cmmn.api.CmmnHistoryService;
import org.flowable.cmmn.api.CmmnRepositoryService;
import org.flowable.cmmn.api.CmmnRuntimeService;
import org.flowable.cmmn.api.history.HistoricCaseInstance;
import org.flowable.cmmn.api.history.HistoricCaseInstanceQuery;
import org.flowable.cmmn.api.repository.CaseDefinition;
import org.flowable.cmmn.api.repository.CmmnDeployment;
import org.flowable.cmmn.api.runtime.CaseInstance;
import org.flowable.cmmn.api.runtime.CaseInstanceQuery;
import org.flowable.cmmn.api.runtime.PlanItemInstance;
import org.flowable.task.api.TaskInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public CaseService(CmmnRuntimeService cmmnRuntimeService,
                       CmmnHistoryService cmmnHistoryService,
                       CmmnRepositoryService cmmnRepositoryService,
                       AuditService auditService) {
        this.cmmnRuntimeService = cmmnRuntimeService;
        this.cmmnHistoryService = cmmnHistoryService;
        this.cmmnRepositoryService = cmmnRepositoryService;
        this.auditService = auditService;
    }

    public List<DefinitionDTO> listCaseDefinitions() {
        String tenantId = MultiTenantFilter.getCurrentTenantId();

        var query = cmmnRepositoryService.createCaseDefinitionQuery().latestVersion();
        if (tenantId != null) {
            query.caseDefinitionTenantId(tenantId);
        }

        List<CaseDefinition> definitions = query.list();

        return definitions.stream()
                .map(this::toDefinitionDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public InstanceDTO startCaseInstance(String caseDefinitionKey, Map<String, Object> variables, String businessKey) {
        String tenantId = MultiTenantFilter.getCurrentTenantId();

        // Verify definition exists for tenant
        var defQuery = cmmnRepositoryService.createCaseDefinitionQuery()
                .caseDefinitionKey(caseDefinitionKey)
                .latestVersion();
        if (tenantId != null) {
            defQuery.caseDefinitionTenantId(tenantId);
        }
        CaseDefinition definition = defQuery.singleResult();
        if (definition == null) {
            throw new IllegalArgumentException("Case definition not found: " + caseDefinitionKey);
        }

        CaseInstance caseInstance;
        if (tenantId != null) {
            caseInstance = cmmnRuntimeService.createCaseInstanceBuilder()
                    .caseDefinitionKey(caseDefinitionKey)
                    .variables(variables != null ? variables : Collections.emptyMap())
                    .businessKey(businessKey)
                    .tenantId(tenantId)
                    .start();
        } else {
            caseInstance = cmmnRuntimeService.createCaseInstanceBuilder()
                    .caseDefinitionKey(caseDefinitionKey)
                    .variables(variables != null ? variables : Collections.emptyMap())
                    .businessKey(businessKey)
                    .start();
        }

        auditService.logAction("CASE_STARTED", "CASE_INSTANCE", caseInstance.getId(), null);

        return toInstanceDTO(caseInstance, definition);
    }

    public List<InstanceDTO> listActiveCaseInstances(int page, int size, String search, String startedBy,
                                                     LocalDateTime startDateFrom, LocalDateTime startDateTo) {
        String tenantId = MultiTenantFilter.getCurrentTenantId();

        CaseInstanceQuery query = cmmnRuntimeService.createCaseInstanceQuery();
        if (tenantId != null) {
            query.caseInstanceTenantId(tenantId);
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

        query.orderByCaseInstanceStartTime().desc();
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
            query.caseInstanceTenantId(tenantId);
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
            query.caseInstanceTenantId(tenantId);
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
            query.caseInstanceTenantId(tenantId);
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
}
