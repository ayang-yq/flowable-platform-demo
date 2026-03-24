package com.flowable.platform.service;

import com.flowable.platform.config.MultiTenantFilter;
import com.flowable.platform.dto.DiagramDataDTO;
import com.flowable.platform.dto.ProcessDTO;
import com.flowable.platform.dto.TaskDTO;
import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.engine.*;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProcessService {

    private final RuntimeService runtimeService;
    private final TaskService taskService;
    private final RepositoryService repositoryService;
    private final HistoryService historyService;
    private final AuditService auditService;
    private final IdentityService identityService;

    public ProcessService(RuntimeService runtimeService,
                         TaskService taskService,
                         RepositoryService repositoryService,
                         HistoryService historyService,
                         AuditService auditService,
                         IdentityService identityService) {
        this.runtimeService = runtimeService;
        this.taskService = taskService;
        this.repositoryService = repositoryService;
        this.historyService = historyService;
        this.auditService = auditService;
        this.identityService = identityService;
    }

    @Transactional
    public ProcessInstance startProcessInstance(String processDefinitionKey, Map<String, Object> variables) {
        // Validate variables size (< 10KB per variable)
        validateVariables(variables);

        // Get tenant context
        String tenantId = MultiTenantFilter.getCurrentTenantId();

        // Find process definition (check tenant-specific first, then global)
        org.flowable.engine.repository.ProcessDefinition definition = null;

        if (tenantId != null) {
            // Try tenant-specific first
            definition = repositoryService.createProcessDefinitionQuery()
                    .processDefinitionKey(processDefinitionKey)
                    .processDefinitionTenantId(tenantId)
                    .latestVersion()
                    .singleResult();
        }

        // Fall back to global (empty tenant) if not found
        if (definition == null) {
            definition = repositoryService.createProcessDefinitionQuery()
                    .processDefinitionKey(processDefinitionKey)
                    .processDefinitionTenantId("")
                    .latestVersion()
                    .singleResult();
        }

        if (definition == null) {
            throw new IllegalArgumentException("Process definition not found: " + processDefinitionKey);
        }

        // Set authenticated user context for Flowable's ${initiator} variable
        String authenticatedUserId = getCurrentUser();
        System.out.println("=== DEBUG ProcessService.startProcessInstance ===");
        System.out.println("Process definition key: " + processDefinitionKey);
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

            // Start process instance with appropriate tenant context
            ProcessInstance processInstance;
            if (tenantId != null && !"".equals(definition.getTenantId())) {
                // Tenant-specific process
                processInstance = runtimeService.createProcessInstanceBuilder()
                        .processDefinitionId(definition.getId())
                        .variables(variablesWithInitiator)
                        .tenantId(tenantId)
                        .start();
            } else {
                // Global process - use the process definition's tenant (empty) or current tenant
                processInstance = runtimeService.createProcessInstanceBuilder()
                        .processDefinitionId(definition.getId())
                        .variables(variablesWithInitiator)
                        .start();
            }

            // Log audit
            auditService.logAction("PROCESS_STARTED", "PROCESS_INSTANCE", processInstance.getId(), null);

            return processInstance;
        } finally {
            // Always clear the authenticated user to avoid thread pool contamination
            identityService.setAuthenticatedUserId(null);
        }
    }

    @Transactional
    public void suspendProcessInstance(String processInstanceId) {
        runtimeService.suspendProcessInstanceById(processInstanceId);
        auditService.logAction("PROCESS_SUSPENDED", "PROCESS_INSTANCE", processInstanceId, null);
    }

    @Transactional
    public void activateProcessInstance(String processInstanceId) {
        runtimeService.activateProcessInstanceById(processInstanceId);
        auditService.logAction("PROCESS_ACTIVATED", "PROCESS_INSTANCE", processInstanceId, null);
    }

    @Transactional
    public void terminateProcessInstance(String processInstanceId, String reason) {
        runtimeService.deleteProcessInstance(processInstanceId, reason);
        auditService.logAction("PROCESS_TERMINATED", "PROCESS_INSTANCE", processInstanceId,
                Map.of("reason", reason));
    }

    public List<ProcessDTO> listProcessInstances() {
        String tenantId = MultiTenantFilter.getCurrentTenantId();

        // Create query - only filter by tenant if tenant context is set
        var query = runtimeService.createProcessInstanceQuery();
        if (tenantId != null) {
            query.processInstanceTenantId(tenantId);
        }

        List<ProcessInstance> instances = query.list();

        return instances.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public ProcessDTO getProcessInstanceDetails(String processInstanceId) {
        ProcessInstance instance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();

        if (instance == null) {
            throw new IllegalArgumentException("Process instance not found: " + processInstanceId);
        }

        return convertToDTO(instance);
    }

    public List<TaskDTO> getTasksForProcessInstance(String processInstanceId) {
        List<Task> tasks = taskService.createTaskQuery()
                .processInstanceId(processInstanceId)
                .list();

        return tasks.stream()
                .map(this::convertTaskToDTO)
                .collect(Collectors.toList());
    }

    private void validateVariables(Map<String, Object> variables) {
        if (variables != null) {
            for (Map.Entry<String, Object> entry : variables.entrySet()) {
                Object value = entry.getValue();
                if (value instanceof String) {
                    String strValue = (String) value;
                    if (strValue.getBytes().length > 10240) { // 10KB limit
                        throw new IllegalArgumentException(
                                "Variable " + entry.getKey() + " exceeds 10KB limit");
                    }
                }
            }
        }
    }

    private ProcessDTO convertToDTO(ProcessInstance instance) {
        ProcessDTO dto = new ProcessDTO();
        dto.setProcessInstanceId(instance.getId());
        dto.setProcessDefinitionKey(instance.getProcessDefinitionKey());
        dto.setBusinessKey(instance.getBusinessKey());
        dto.setSuspended(instance.isSuspended());
        dto.setTenantId(instance.getTenantId());

        // Convert start time
        if (instance.getStartTime() != null) {
            dto.setStartTime(LocalDateTime.ofInstant(
                    instance.getStartTime().toInstant(),
                    ZoneId.systemDefault()
            ));
        }

        // Get variables
        Map<String, Object> variables = runtimeService.getVariables(instance.getId());
        dto.setVariables(variables);

        return dto;
    }

    private TaskDTO convertTaskToDTO(Task task) {
        TaskDTO dto = new TaskDTO();
        dto.setId(task.getId());
        dto.setName(task.getName());
        dto.setDescription(task.getDescription());
        dto.setAssignee(task.getAssignee());
        dto.setProcessInstanceId(task.getProcessInstanceId());
        dto.setCreateTime(task.getCreateTime() != null ?
                LocalDateTime.ofInstant(task.getCreateTime().toInstant(), ZoneId.systemDefault()) : null);
        dto.setDueDate(task.getDueDate() != null ?
                LocalDateTime.ofInstant(task.getDueDate().toInstant(), ZoneId.systemDefault()) : null);
        dto.setPriority(task.getPriority());
        dto.setCategory(task.getCategory());

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

    public DiagramDataDTO getProcessInstanceDiagram(String processInstanceId) {
        // Get process instance
        ProcessInstance instance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();

        if (instance == null) {
            return null;
        }

        // Get process definition
        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(instance.getProcessDefinitionId())
                .singleResult();

        if (definition == null) {
            return null;
        }

        // Get BPMN model and convert to XML
        BpmnModel bpmnModel = repositoryService.getBpmnModel(definition.getId());
        BpmnXMLConverter xmlConverter = new BpmnXMLConverter();
        byte[] xmlBytes = xmlConverter.convertToXML(bpmnModel);
        String diagramXml = new String(xmlBytes);

        // Calculate active and completed elements
        List<String> activeElementIds = new ArrayList<>();
        List<String> completedElementIds = new ArrayList<>();
        String currentElementId = null;

        // Get active tasks (user tasks, service tasks currently executing)
        List<org.flowable.task.api.Task> activeTasks = taskService.createTaskQuery()
                .processInstanceId(processInstanceId)
                .list();

        for (org.flowable.task.api.Task task : activeTasks) {
            String taskDefinitionKey = task.getTaskDefinitionKey();
            if (taskDefinitionKey != null) {
                activeElementIds.add(taskDefinitionKey);
                // Use first active task as current element
                if (currentElementId == null) {
                    currentElementId = taskDefinitionKey;
                }
            }
        }

        // Get active executions (for parallel gateways, service tasks, etc.)
        List<org.flowable.engine.runtime.Execution> activeExecutions = runtimeService.createExecutionQuery()
                .processInstanceId(processInstanceId)
                .list();

        for (org.flowable.engine.runtime.Execution execution : activeExecutions) {
            String activityId = execution.getActivityId();
            if (activityId != null && !activeElementIds.contains(activityId)) {
                activeElementIds.add(activityId);
            }
        }

        // Get completed activities from history
        List<org.flowable.engine.history.HistoricActivityInstance> completedActivities =
                historyService.createHistoricActivityInstanceQuery()
                        .processInstanceId(processInstanceId)
                        .finished()
                        .list();

        for (org.flowable.engine.history.HistoricActivityInstance activity : completedActivities) {
            String activityId = activity.getActivityId();
            if (activityId != null && !completedElementIds.contains(activityId)) {
                completedElementIds.add(activityId);
            }
        }

        // Create diagram data DTO
        DiagramDataDTO diagramData = new DiagramDataDTO();
        diagramData.setDiagramXml(diagramXml);
        diagramData.setActiveElementIds(activeElementIds);
        diagramData.setCompletedElementIds(completedElementIds);
        diagramData.setCurrentElementId(currentElementId);

        return diagramData;
    }
}
