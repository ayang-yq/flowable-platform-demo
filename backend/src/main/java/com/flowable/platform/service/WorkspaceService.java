package com.flowable.platform.service;

import com.flowable.platform.config.MultiTenantFilter;
import com.flowable.platform.dto.*;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.history.HistoricProcessInstanceQuery;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.runtime.ProcessInstanceQuery;
import org.flowable.task.api.Task;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class WorkspaceService {

    private final RuntimeService runtimeService;
    private final RepositoryService repositoryService;
    private final HistoryService historyService;
    private final org.flowable.engine.TaskService taskService;
    private final CaseService caseService;
    private final DecisionService decisionService;

    public WorkspaceService(RuntimeService runtimeService,
                            RepositoryService repositoryService,
                            HistoryService historyService,
                            org.flowable.engine.TaskService taskService,
                            CaseService caseService,
                            DecisionService decisionService) {
        this.runtimeService = runtimeService;
        this.repositoryService = repositoryService;
        this.historyService = historyService;
        this.taskService = taskService;
        this.caseService = caseService;
        this.decisionService = decisionService;
    }

    public List<DefinitionDTO> getAllDefinitions(String typeFilter) {
        List<DefinitionDTO> all = new ArrayList<>();

        if (typeFilter == null || "all".equalsIgnoreCase(typeFilter) || "BPMN".equalsIgnoreCase(typeFilter)) {
            all.addAll(listBpmnDefinitions());
        }
        if (typeFilter == null || "all".equalsIgnoreCase(typeFilter) || "CMMN".equalsIgnoreCase(typeFilter)) {
            all.addAll(caseService.listCaseDefinitions());
        }
        if (typeFilter == null || "all".equalsIgnoreCase(typeFilter) || "DMN".equalsIgnoreCase(typeFilter)) {
            all.addAll(decisionService.listDecisionDefinitions());
        }

        all.sort(Comparator.comparing(d -> d.getName() != null ? d.getName() : d.getKey()));
        return all;
    }

    public InstancePageDTO getActiveInstances(int page, int size, String typeFilter, String search,
                                               String startedBy, LocalDateTime startDateFrom, LocalDateTime startDateTo) {
        List<InstanceDTO> bpmnInstances = new ArrayList<>();
        List<InstanceDTO> cmmnInstances = new ArrayList<>();
        long bpmnCount = 0;
        long cmmnCount = 0;

        boolean includeBpmn = typeFilter == null || "all".equalsIgnoreCase(typeFilter) || "BPMN".equalsIgnoreCase(typeFilter);
        boolean includeCmmn = typeFilter == null || "all".equalsIgnoreCase(typeFilter) || "CMMN".equalsIgnoreCase(typeFilter);

        if (includeBpmn) {
            bpmnInstances = listActiveBpmnInstances(page, size, search, startedBy, startDateFrom, startDateTo);
            bpmnCount = countActiveBpmnInstances(startedBy);
        }
        if (includeCmmn) {
            cmmnInstances = caseService.listActiveCaseInstances(page, size, search, startedBy, startDateFrom, startDateTo);
            cmmnCount = caseService.countActiveCaseInstances(startedBy);
        }

        // Merge and sort by startTime desc
        List<InstanceDTO> merged = Stream.concat(bpmnInstances.stream(), cmmnInstances.stream())
                .sorted(Comparator.comparing(InstanceDTO::getStartTime, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());

        // Apply page size limit to merged results
        if (merged.size() > size) {
            merged = merged.subList(0, size);
        }

        long totalElements = bpmnCount + cmmnCount;
        return new InstancePageDTO(merged, totalElements, size, page);
    }

    public InstancePageDTO getCompletedInstances(int page, int size, String typeFilter, String search,
                                                  String startedBy, InstanceStatus statusFilter,
                                                  LocalDateTime startDateFrom, LocalDateTime startDateTo,
                                                  LocalDateTime endDateFrom, LocalDateTime endDateTo) {
        List<InstanceDTO> bpmnInstances = new ArrayList<>();
        List<InstanceDTO> cmmnInstances = new ArrayList<>();
        long bpmnCount = 0;
        long cmmnCount = 0;

        boolean includeBpmn = typeFilter == null || "all".equalsIgnoreCase(typeFilter) || "BPMN".equalsIgnoreCase(typeFilter);
        boolean includeCmmn = typeFilter == null || "all".equalsIgnoreCase(typeFilter) || "CMMN".equalsIgnoreCase(typeFilter);

        if (includeBpmn) {
            bpmnInstances = listCompletedBpmnInstances(page, size, search, startedBy, statusFilter,
                    startDateFrom, startDateTo, endDateFrom, endDateTo);
            bpmnCount = countCompletedBpmnInstances();
        }
        if (includeCmmn) {
            cmmnInstances = caseService.listCompletedCaseInstances(page, size, search, startedBy, statusFilter,
                    startDateFrom, startDateTo, endDateFrom, endDateTo);
            cmmnCount = caseService.countCompletedCaseInstances();
        }

        List<InstanceDTO> merged = Stream.concat(bpmnInstances.stream(), cmmnInstances.stream())
                .sorted(Comparator.comparing(InstanceDTO::getEndTime, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());

        if (merged.size() > size) {
            merged = merged.subList(0, size);
        }

        long totalElements = bpmnCount + cmmnCount;
        return new InstancePageDTO(merged, totalElements, size, page);
    }

    public InstanceDetailDTO getInstanceDetail(String instanceId, String type) {
        if ("CMMN".equalsIgnoreCase(type)) {
            return caseService.getCaseInstanceDetail(instanceId);
        }
        // Default to BPMN
        return getBpmnInstanceDetail(instanceId);
    }

    public DashboardSummaryDTO getDashboardSummary() {
        String tenantId = MultiTenantFilter.getCurrentTenantId();
        String currentUser = getCurrentUsername();

        // Active counts
        long activeBpmn = countActiveBpmnInstances(null);
        long activeCmmn = caseService.countActiveCaseInstances(null);
        long activeCount = activeBpmn + activeCmmn;

        // Completed counts
        long completedBpmn = countCompletedBpmnInstances();
        long completedCmmn = caseService.countCompletedCaseInstances();
        long completedCount = completedBpmn + completedCmmn;

        // Started today
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        Date startOfDayDate = Date.from(startOfDay.atZone(ZoneId.systemDefault()).toInstant());

        var bpmnTodayQuery = historyService.createHistoricProcessInstanceQuery()
                .startedAfter(startOfDayDate);
        if (tenantId != null) {
            bpmnTodayQuery.processInstanceTenantId(tenantId);
        }
        long startedTodayBpmn = bpmnTodayQuery.count();

        long startedTodayCount = startedTodayBpmn;

        // My active counts
        long myActiveBpmn = countActiveBpmnInstances(currentUser);
        long myActiveCmmn = caseService.countActiveCaseInstances(currentUser);
        long myActiveCount = myActiveBpmn + myActiveCmmn;

        return new DashboardSummaryDTO(activeCount, completedCount, startedTodayCount, myActiveCount);
    }

    // === BPMN-specific methods ===

    private List<DefinitionDTO> listBpmnDefinitions() {
        String tenantId = MultiTenantFilter.getCurrentTenantId();

        // Get both tenant-specific AND global (empty tenant) process definitions
        // Global processes are auto-deployed from classpath and available to all tenants
        List<ProcessDefinition> tenantDefinitions = new ArrayList<>();
        List<ProcessDefinition> globalDefinitions = new ArrayList<>();

        // Query tenant-specific definitions
        if (tenantId != null) {
            tenantDefinitions = repositoryService.createProcessDefinitionQuery()
                    .processDefinitionTenantId(tenantId)
                    .latestVersion()
                    .list();
        }

        // Query global definitions (empty string = auto-deployed processes)
        globalDefinitions = repositoryService.createProcessDefinitionQuery()
                .processDefinitionTenantId("")
                .latestVersion()
                .list();

        // Merge both lists, removing duplicates (in case a process exists in both)
        List<ProcessDefinition> allDefinitions = new ArrayList<>(tenantDefinitions);
        for (ProcessDefinition globalDef : globalDefinitions) {
            // Only add global definition if not already present in tenant-specific list
            boolean exists = tenantDefinitions.stream()
                    .anyMatch(def -> def.getKey().equals(globalDef.getKey()));
            if (!exists) {
                allDefinitions.add(globalDef);
            }
        }

        return allDefinitions.stream()
                .map(this::toBpmnDefinitionDTO)
                .collect(Collectors.toList());
    }

    private List<InstanceDTO> listActiveBpmnInstances(int page, int size, String search, String startedBy,
                                                       LocalDateTime startDateFrom, LocalDateTime startDateTo) {
        String tenantId = MultiTenantFilter.getCurrentTenantId();

        ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();
        if (tenantId != null) {
            // Include both tenant-specific instances AND global instances (no tenant ID)
            query.or()
                   .processInstanceTenantId(tenantId)
                   .processInstanceTenantId("")  // Empty string = global instances
               .endOr();
        }
        if (startedBy != null && !startedBy.isBlank()) {
            query.startedBy(startedBy);
        }
        if (search != null && !search.isBlank()) {
            query.processInstanceNameLikeIgnoreCase("%" + search + "%");
        }

        query.orderByProcessInstanceId().desc();
        List<ProcessInstance> instances = query.listPage(page * size, size);

        return instances.stream()
                .map(this::toBpmnInstanceDTO)
                .collect(Collectors.toList());
    }

    private long countActiveBpmnInstances(String startedBy) {
        String tenantId = MultiTenantFilter.getCurrentTenantId();
        ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();
        if (tenantId != null) {
            // Include both tenant-specific instances AND global instances (no tenant ID)
            query.or()
                   .processInstanceTenantId(tenantId)
                   .processInstanceTenantId("")  // Empty string = global instances
               .endOr();
        }
        if (startedBy != null && !startedBy.isBlank()) {
            query.startedBy(startedBy);
        }
        return query.count();
    }

    private List<InstanceDTO> listCompletedBpmnInstances(int page, int size, String search, String startedBy,
                                                          InstanceStatus statusFilter,
                                                          LocalDateTime startDateFrom, LocalDateTime startDateTo,
                                                          LocalDateTime endDateFrom, LocalDateTime endDateTo) {
        String tenantId = MultiTenantFilter.getCurrentTenantId();

        HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery()
                .finished();
        if (tenantId != null) {
            // Include both tenant-specific instances AND global instances (no tenant ID)
            query.or()
                   .processInstanceTenantId(tenantId)
                   .processInstanceTenantId("")  // Empty string = global instances
               .endOr();
        }
        if (startedBy != null && !startedBy.isBlank()) {
            query.startedBy(startedBy);
        }
        if (search != null && !search.isBlank()) {
            query.processInstanceNameLikeIgnoreCase("%" + search + "%");
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

        query.orderByProcessInstanceEndTime().desc();
        List<HistoricProcessInstance> instances = query.listPage(page * size, size);

        return instances.stream()
                .map(this::toBpmnInstanceDTOFromHistory)
                .collect(Collectors.toList());
    }

    private long countCompletedBpmnInstances() {
        String tenantId = MultiTenantFilter.getCurrentTenantId();
        HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery().finished();
        if (tenantId != null) {
            // Include both tenant-specific instances AND global instances (no tenant ID)
            query.or()
                   .processInstanceTenantId(tenantId)
                   .processInstanceTenantId("")  // Empty string = global instances
               .endOr();
        }
        return query.count();
    }

    private InstanceDetailDTO getBpmnInstanceDetail(String processInstanceId) {
        // Try active first
        ProcessInstance active = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();

        if (active != null) {
            InstanceDetailDTO detail = new InstanceDetailDTO();
            copyToDetail(detail, toBpmnInstanceDTO(active));
            detail.setVariables(runtimeService.getVariables(processInstanceId));

            // Current activities
            List<Execution> executions = runtimeService.createExecutionQuery()
                    .processInstanceId(processInstanceId)
                    .onlyChildExecutions()
                    .list();
            List<String> activityNames = executions.stream()
                    .map(Execution::getActivityId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            detail.setCurrentActivities(activityNames);

            // Active tasks
            List<Task> tasks = taskService.createTaskQuery()
                    .processInstanceId(processInstanceId)
                    .list();
            detail.setTasks(tasks.stream().map(this::toTaskDTO).collect(Collectors.toList()));

            return detail;
        }

        // Try historic
        HistoricProcessInstance historic = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();

        if (historic == null) {
            throw new IllegalArgumentException("Process instance not found: " + processInstanceId);
        }

        InstanceDetailDTO detail = new InstanceDetailDTO();
        copyToDetail(detail, toBpmnInstanceDTOFromHistory(historic));
        return detail;
    }

    // === DTO converters ===

    private DefinitionDTO toBpmnDefinitionDTO(ProcessDefinition def) {
        LocalDateTime deploymentTime = null;
        if (def.getDeploymentId() != null) {
            Deployment deployment = repositoryService.createDeploymentQuery()
                    .deploymentId(def.getDeploymentId())
                    .singleResult();
            if (deployment != null && deployment.getDeploymentTime() != null) {
                deploymentTime = deployment.getDeploymentTime().toInstant()
                        .atZone(ZoneId.systemDefault()).toLocalDateTime();
            }
        }

        return new DefinitionDTO(
                def.getId(), def.getKey(), def.getName(), def.getVersion(),
                def.getCategory(), DefinitionType.BPMN, def.hasStartFormKey(), deploymentTime
        );
    }

    private InstanceDTO toBpmnInstanceDTO(ProcessInstance pi) {
        InstanceDTO dto = new InstanceDTO();
        dto.setId(pi.getId());
        dto.setDefinitionId(pi.getProcessDefinitionId());
        dto.setDefinitionKey(pi.getProcessDefinitionKey());
        dto.setDefinitionName(pi.getProcessDefinitionName());
        dto.setType(DefinitionType.BPMN);
        dto.setStartTime(pi.getStartTime() != null ?
                pi.getStartTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime() : null);
        dto.setStartedBy(pi.getStartUserId());
        dto.setStatus(pi.isSuspended() ? InstanceStatus.SUSPENDED : InstanceStatus.ACTIVE);
        dto.setBusinessKey(pi.getBusinessKey());
        dto.setTenantId(pi.getTenantId());
        return dto;
    }

    private InstanceDTO toBpmnInstanceDTOFromHistory(HistoricProcessInstance hpi) {
        InstanceDTO dto = new InstanceDTO();
        dto.setId(hpi.getId());
        dto.setDefinitionId(hpi.getProcessDefinitionId());
        dto.setDefinitionKey(hpi.getProcessDefinitionKey());
        dto.setDefinitionName(hpi.getProcessDefinitionName());
        dto.setType(DefinitionType.BPMN);
        dto.setStartTime(hpi.getStartTime() != null ?
                hpi.getStartTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime() : null);
        dto.setEndTime(hpi.getEndTime() != null ?
                hpi.getEndTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime() : null);
        if (hpi.getStartTime() != null && hpi.getEndTime() != null) {
            dto.setDuration(hpi.getEndTime().getTime() - hpi.getStartTime().getTime());
        }
        dto.setStartedBy(hpi.getStartUserId());
        dto.setStatus(mapBpmnState(hpi.getDeleteReason()));
        dto.setBusinessKey(hpi.getBusinessKey());
        dto.setTenantId(hpi.getTenantId());
        return dto;
    }

    private InstanceStatus mapBpmnState(String deleteReason) {
        if (deleteReason == null) return InstanceStatus.COMPLETED;
        if (deleteReason.contains("cancel") || deleteReason.contains("terminat")) {
            return InstanceStatus.CANCELLED;
        }
        return InstanceStatus.COMPLETED;
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

    private void copyToDetail(InstanceDetailDTO detail, InstanceDTO source) {
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

    private String getCurrentUsername() {
        try {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception e) {
            return null;
        }
    }
}
