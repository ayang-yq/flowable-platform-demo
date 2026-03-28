package com.flowable.platform.service;

import com.flowable.platform.config.MultiTenantFilter;
import com.flowable.platform.dto.TaskDTO;
import com.flowable.platform.entity.User;
import com.flowable.platform.repository.UserRepository;
import org.flowable.cmmn.api.CmmnTaskService;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing workflow tasks including claiming, completing, delegating,
 * and CC (carbon copy) functionality with business calendar integration.
 */
@Service
@Transactional
public class TaskManagementService {

    private final org.flowable.engine.TaskService flowableTaskService;
    private final RuntimeService runtimeService;
    private final HistoryService historyService;
    private final UserRepository userRepository;
    private final BusinessCalendarService businessCalendarService;
    private final AuditService auditService;

    private final CmmnTaskService cmmnTaskService;

    @Autowired
    public TaskManagementService(
            org.flowable.engine.TaskService flowableTaskService,
            CmmnTaskService cmmnTaskService,
            RuntimeService runtimeService,
            HistoryService historyService,
            UserRepository userRepository,
            BusinessCalendarService businessCalendarService,
            AuditService auditService) {
        this.flowableTaskService = flowableTaskService;
        this.cmmnTaskService = cmmnTaskService;
        this.runtimeService = runtimeService;
        this.historyService = historyService;
        this.userRepository = userRepository;
        this.businessCalendarService = businessCalendarService;
        this.auditService = auditService;
    }

    /**
     * Get tasks assigned to the current user
     */
    public Page<TaskDTO> getMyTasks(String department, String priority, LocalDate dueBefore, Pageable pageable) {
        String currentUsername = getCurrentUsername();

        List<Task> tasks = flowableTaskService.createTaskQuery()
                .taskAssignee(currentUsername)
                .orderByTaskCreateTime()
                .desc()
                .listPage((int) pageable.getOffset(), pageable.getPageSize() + 1); // +1 to check if there are more

        // Apply filters
        List<TaskDTO> filteredTasks = tasks.stream()
                .limit(pageable.getPageSize())
                .map(this::convertToDTO)
                .filter(taskDTO -> applyFilters(taskDTO, department, priority, dueBefore))
                .collect(Collectors.toList());

        return new PageImpl<>(filteredTasks, pageable, filteredTasks.size());
    }

    /**
     * Get completed tasks for the current user
     */
    public Page<TaskDTO> getCompletedTasks(Pageable pageable) {
        String currentUsername = getCurrentUsername();

        List<HistoricTaskInstance> completedTasks = historyService.createHistoricTaskInstanceQuery()
                .taskAssignee(currentUsername)
                .finished()
                .orderByHistoricTaskInstanceEndTime()
                .desc()
                .listPage((int) pageable.getOffset(), pageable.getPageSize() + 1);

        List<TaskDTO> taskDTOs = completedTasks.stream()
                .limit(pageable.getPageSize())
                .map(this::convertFromHistoric)
                .collect(Collectors.toList());

        return new PageImpl<>(taskDTOs, pageable, taskDTOs.size());
    }

    /**
     * Get process instances initiated by the current user
     */
    public Page<TaskDTO> getMyRequests(Pageable pageable) {
        String currentUsername = getCurrentUsername();

        List<ProcessInstance> instances = runtimeService.createProcessInstanceQuery()
                .startedBy(currentUsername)
                .orderByStartTime()
                .desc()
                .listPage((int) pageable.getOffset(), pageable.getPageSize() + 1);

        List<TaskDTO> requestDTOs = instances.stream()
                .limit(pageable.getPageSize())
                .map(this::convertFromProcessInstance)
                .collect(Collectors.toList());

        return new PageImpl<>(requestDTOs, pageable, requestDTOs.size());
    }

    /**
     * Get all tasks (admin only)
     */
    public Page<TaskDTO> getAllTasks(Pageable pageable) {
        List<Task> tasks = flowableTaskService.createTaskQuery()
                .orderByTaskCreateTime()
                .desc()
                .listPage((int) pageable.getOffset(), pageable.getPageSize() + 1);

        List<TaskDTO> taskDTOs = tasks.stream()
                .limit(pageable.getPageSize())
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(taskDTOs, pageable, taskDTOs.size());
    }

    /**
     * Get task by ID
     */
    public TaskDTO getTaskById(String taskId) {
        Task task = flowableTaskService.createTaskQuery()
                .taskId(taskId)
                .singleResult();

        if (task == null) {
            throw new RuntimeException("Task not found: " + taskId);
        }

        return convertToDTO(task);
    }

    /**
     * Claim an unassigned task
     */
    public TaskDTO claimTask(String taskId) {
        Task task = flowableTaskService.createTaskQuery()
                .taskId(taskId)
                .singleResult();

        if (task == null) {
            throw new RuntimeException("Task not found: " + taskId);
        }

        if (task.getAssignee() != null && !task.getAssignee().isEmpty()) {
            throw new IllegalStateException("Task is already assigned to: " + task.getAssignee());
        }

        String currentUsername = getCurrentUsername();
        flowableTaskService.setAssignee(taskId, currentUsername);

        // Audit log
        auditService.logAction("TASK_CLAIMED", "Task", taskId,
                Map.of("assignee", currentUsername));

        return getTaskById(taskId);
    }

    /**
     * Complete a task with variables
     */
    public void completeTask(String taskId, Map<String, Object> variables) {
        Task task = flowableTaskService.createTaskQuery()
                .taskId(taskId)
                .singleResult();

        if (task == null) {
            throw new RuntimeException("Task not found: " + taskId);
        }

        String currentUsername = getCurrentUsername();

        // Auto-claim unassigned tasks
        if (task.getAssignee() == null || task.getAssignee().isEmpty()) {
            flowableTaskService.setAssignee(taskId, currentUsername);
        } else if (!currentUsername.equals(task.getAssignee()) && !isCurrentUserAdmin()) {
            throw new IllegalStateException("Task is not assigned to current user");
        }

        // Validate variable sizes
        if (variables != null) {
            validateVariables(variables);
        }

        // Complete via CMMN or BPMN engine depending on task scope
        if ("cmmn".equals(task.getScopeType())) {
            // CMMN task - use CMMN task service
            cmmnTaskService.complete(taskId, variables);
        } else {
            // BPMN task - use standard task service
            flowableTaskService.complete(taskId, variables);
        }

        // Audit log
        auditService.logAction("TASK_COMPLETED", "Task", taskId,
                variables != null ? variables : Map.of());
    }

    /**
     * Delegate task to another user
     */
    public void delegateTask(String taskId, String delegateTo) {
        Task task = flowableTaskService.createTaskQuery()
                .taskId(taskId)
                .singleResult();

        if (task == null) {
            throw new RuntimeException("Task not found: " + taskId);
        }

        String currentUsername = getCurrentUsername();

        if (!currentUsername.equals(task.getAssignee())) {
            throw new IllegalStateException("Task is not assigned to current user");
        }

        // Set owner to current user and assignee to delegate user
        flowableTaskService.setOwner(taskId, currentUsername);
        flowableTaskService.setAssignee(taskId, delegateTo);

        // Audit log
        auditService.logAction("TASK_DELEGATED", "Task", taskId,
                Map.of("from", currentUsername, "to", delegateTo));
    }

    /**
     * Reassign task (admin only)
     */
    public void reassignTask(String taskId, String assignee) {
        Task task = flowableTaskService.createTaskQuery()
                .taskId(taskId)
                .singleResult();

        if (task == null) {
            throw new RuntimeException("Task not found: " + taskId);
        }

        String currentUsername = getCurrentUsername();

        flowableTaskService.setAssignee(taskId, assignee);

        // Audit log
        auditService.logAction("TASK_REASSIGNED", "Task", taskId,
                Map.of("assignee", assignee, "admin", currentUsername));
    }

    /**
     * Add CC users to a task
     */
    public void addCcUsers(String taskId, List<String> ccUsers) {
        Task task = flowableTaskService.createTaskQuery()
                .taskId(taskId)
                .singleResult();

        if (task == null) {
            throw new RuntimeException("Task not found: " + taskId);
        }

        // Get existing CC users
        @SuppressWarnings("unchecked")
        List<String> existingCcUsers = (List<String>) flowableTaskService.getVariable(taskId, "ccUsers");
        if (existingCcUsers == null) {
            existingCcUsers = new ArrayList<>();
        }

        // Add new CC users
        List<String> updatedCcUsers = new ArrayList<>(existingCcUsers);
        for (String username : ccUsers) {
            if (!updatedCcUsers.contains(username)) {
                updatedCcUsers.add(username);
            }
        }

        flowableTaskService.setVariable(taskId, "ccUsers", updatedCcUsers);

        // Audit log
        String currentUsername = getCurrentUsername();
        auditService.logAction("TASK_CC_ADDED", "Task", taskId,
                Map.of("addedUsers", ccUsers, "currentCcUsers", updatedCcUsers));
    }

    /**
     * Remove CC user from a task
     */
    public void removeCcUser(String taskId, String username) {
        Task task = flowableTaskService.createTaskQuery()
                .taskId(taskId)
                .singleResult();

        if (task == null) {
            throw new RuntimeException("Task not found: " + taskId);
        }

        @SuppressWarnings("unchecked")
        List<String> ccUsers = (List<String>) flowableTaskService.getVariable(taskId, "ccUsers");

        if (ccUsers != null && ccUsers.contains(username)) {
            ccUsers.remove(username);
            flowableTaskService.setVariable(taskId, "ccUsers", ccUsers);

            // Audit log
            String currentUsername = getCurrentUsername();
            auditService.logAction("TASK_CC_REMOVED", "Task", taskId,
                Map.of("removedUser", username, "remainingCcUsers", ccUsers));
        }
    }

    /**
     * Get CC users for a task
     */
    public List<String> getCcUsers(String taskId) {
        Task task = flowableTaskService.createTaskQuery()
                .taskId(taskId)
                .singleResult();

        if (task == null) {
            throw new RuntimeException("Task not found: " + taskId);
        }

        @SuppressWarnings("unchecked")
        List<String> ccUsers = (List<String>) flowableTaskService.getVariable(taskId, "ccUsers");

        return ccUsers != null ? ccUsers : List.of();
    }

    /**
     * Calculate and set task due date using business calendar
     */
    public void setTaskDueDateWithBusinessCalendar(String taskId, int businessDays) {
        Task task = flowableTaskService.createTaskQuery()
                .taskId(taskId)
                .singleResult();

        if (task == null) {
            throw new RuntimeException("Task not found: " + taskId);
        }

        LocalDateTime createdDate = task.getCreateTime().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        LocalDateTime dueDate = businessCalendarService.calculateDueDate(createdDate, businessDays);

        flowableTaskService.setDueDate(taskId, Date.from(dueDate.atZone(ZoneId.systemDefault()).toInstant()));

        // Audit log
        auditService.logAction("TASK_DUE_DATE_SET", "Task", taskId,
                Map.of("dueDate", dueDate.toString(), "businessDays", businessDays));
    }

    /**
     * Check if task is overdue
     */
    public boolean isTaskOverdue(String taskId) {
        Task task = flowableTaskService.createTaskQuery()
                .taskId(taskId)
                .singleResult();

        if (task == null) {
            throw new RuntimeException("Task not found: " + taskId);
        }

        if (task.getDueDate() == null) {
            return false;
        }

        return businessCalendarService.isOverdue(
                task.getDueDate().toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime()
        );
    }

    /**
     * Get tasks with expiration alerts (approaching or past due date)
     */
    public List<TaskDTO> getTasksWithExpirationAlerts() {
        String currentUsername = getCurrentUsername();

        List<Task> allTasks = flowableTaskService.createTaskQuery()
                .taskAssignee(currentUsername)
                .list();

        List<TaskDTO> alertTasks = new ArrayList<>();

        for (Task task : allTasks) {
            TaskDTO taskDTO = convertToDTO(task);

            if (task.getDueDate() != null) {
                LocalDateTime dueDate = task.getDueDate().toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime();

                if (businessCalendarService.isOverdue(dueDate) ||
                    businessCalendarTimeUntilDue(dueDate) <= 1) { // 1 business day or less
                    alertTasks.add(taskDTO);
                }
            }
        }

        return alertTasks;
    }

    // Private helper methods

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : "system";
    }

    private boolean isCurrentUserAdmin() {
        String username = getCurrentUsername();
        String tenantId = MultiTenantFilter.getCurrentTenantId();
        if (tenantId == null) {
            return false;
        }
        return userRepository.findActiveByTenantIdAndUsernameWithRoles(
                        UUID.fromString(tenantId), username)
                .map(user -> user.getRoles().stream()
                        .anyMatch(role -> "ADMIN".equalsIgnoreCase(role.getCode())))
                .orElse(false);
    }

    private TaskDTO convertToDTO(Task task) {
        TaskDTO dto = new TaskDTO();
        dto.setId(task.getId());
        dto.setName(task.getName());
        dto.setDescription(task.getDescription());
        dto.setAssignee(task.getAssignee());
        dto.setProcessInstanceId(task.getProcessInstanceId());
        dto.setProcessDefinitionKey(task.getProcessDefinitionId());
        dto.setCreateTime(task.getCreateTime() != null ?
                task.getCreateTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime() : null);
        dto.setDueDate(task.getDueDate() != null ?
                task.getDueDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime() : null);
        dto.setPriority(task.getPriority());
        dto.setCategory(task.getCategory());

        // Get CC users
        @SuppressWarnings("unchecked")
        List<String> ccUsers = (List<String>) flowableTaskService.getVariable(task.getId(), "ccUsers");
        dto.setCcUsers(ccUsers != null ? ccUsers : List.of());

        return dto;
    }

    private TaskDTO convertFromHistoric(HistoricTaskInstance historicTask) {
        TaskDTO dto = new TaskDTO();
        dto.setId(historicTask.getId());
        dto.setName(historicTask.getName());
        dto.setDescription(historicTask.getDescription());
        dto.setAssignee(historicTask.getAssignee());
        dto.setProcessInstanceId(historicTask.getProcessInstanceId());
        dto.setCreateTime(historicTask.getCreateTime() != null ?
                historicTask.getCreateTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime() : null);
        dto.setDueDate(historicTask.getDueDate() != null ?
                historicTask.getDueDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime() : null);
        dto.setPriority(historicTask.getPriority());
        dto.setCategory(historicTask.getCategory());

        return dto;
    }

    private TaskDTO convertFromProcessInstance(ProcessInstance instance) {
        TaskDTO dto = new TaskDTO();
        dto.setId(instance.getId());
        dto.setProcessInstanceId(instance.getId());
        dto.setProcessDefinitionKey(instance.getProcessDefinitionKey());
        dto.setCreateTime(instance.getStartTime() != null ?
                instance.getStartTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime() : null);

        return dto;
    }

    private boolean applyFilters(TaskDTO task, String department, String priority, LocalDate dueBefore) {
        if (department != null && !department.isEmpty()) {
            // Add department filtering logic here when you have department info on tasks
            // For now, this is a placeholder
        }

        if (priority != null && !priority.isEmpty()) {
            int taskPriority = task.getPriority();
            switch (priority.toLowerCase()) {
                case "high":
                    if (taskPriority < 70) return false;
                    break;
                case "medium":
                    if (taskPriority < 40 || taskPriority >= 70) return false;
                    break;
                case "low":
                    if (taskPriority >= 40) return false;
                    break;
            }
        }

        if (dueBefore != null && task.getDueDate() != null) {
            if (task.getDueDate().toLocalDate().isAfter(dueBefore)) {
                return false;
            }
        }

        return true;
    }

    private void validateVariables(Map<String, Object> variables) {
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            if (entry.getValue() instanceof String) {
                String value = (String) entry.getValue();
                if (value.length() > 10000) { // 10KB limit
                    throw new IllegalArgumentException(
                            "Variable " + entry.getKey() + " exceeds maximum size of 10KB");
                }
            }
        }
    }

    private long businessCalendarTimeUntilDue(LocalDateTime dueDate) {
        LocalDateTime now = LocalDateTime.now();
        return businessCalendarService.calculateBusinessDays(now.toLocalDate(), dueDate.toLocalDate());
    }
}
