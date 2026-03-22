package com.flowable.platform.controller;

import com.flowable.platform.dto.ApiResponse;
import com.flowable.platform.dto.TaskDTO;
import com.flowable.platform.service.TaskManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * REST controller for task management operations.
 * Provides endpoints for claiming, completing, delegating, and managing tasks with CC functionality.
 */
@RestController
@RequestMapping("/api/tasks")
@Tag(name = "Task Management", description = "APIs for managing workflow tasks")
@SecurityRequirement(name = "bearerAuth")
public class TaskController {

    private final TaskManagementService taskManagementService;

    @Autowired
    public TaskController(TaskManagementService taskManagementService) {
        this.taskManagementService = taskManagementService;
    }

    @GetMapping("/my-tasks")
    @Operation(summary = "Get my tasks", description = "Retrieve tasks assigned to the current user")
    public ResponseEntity<ApiResponse<Page<TaskDTO>>> getMyTasks(
            @Parameter(description = "Department filter") @RequestParam(required = false) String department,
            @Parameter(description = "Priority filter (high/medium/low)") @RequestParam(required = false) String priority,
            @Parameter(description = "Due date filter (before this date)") @RequestParam(required = false) LocalDate dueBefore,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<TaskDTO> tasks = taskManagementService.getMyTasks(department, priority, dueBefore, pageable);

        return ResponseEntity.ok(ApiResponse.success(tasks));
    }

    @GetMapping("/completed")
    @Operation(summary = "Get completed tasks", description = "Retrieve tasks completed by the current user")
    public ResponseEntity<ApiResponse<Page<TaskDTO>>> getCompletedTasks(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<TaskDTO> tasks = taskManagementService.getCompletedTasks(pageable);

        return ResponseEntity.ok(ApiResponse.success(tasks));
    }

    @GetMapping("/my-requests")
    @Operation(summary = "Get my requests", description = "Retrieve process instances initiated by the current user")
    public ResponseEntity<ApiResponse<Page<TaskDTO>>> getMyRequests(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<TaskDTO> requests = taskManagementService.getMyRequests(pageable);

        return ResponseEntity.ok(ApiResponse.success(requests));
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all tasks", description = "Retrieve all tasks (admin only)")
    public ResponseEntity<ApiResponse<Page<TaskDTO>>> getAllTasks(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<TaskDTO> tasks = taskManagementService.getAllTasks(pageable);

        return ResponseEntity.ok(ApiResponse.success(tasks));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get task details", description = "Retrieve detailed information about a specific task")
    public ResponseEntity<ApiResponse<TaskDTO>> getTask(
            @Parameter(description = "Task ID") @PathVariable String id) {

        TaskDTO task = taskManagementService.getTaskById(id);
        return ResponseEntity.ok(ApiResponse.success(task));
    }

    @PostMapping("/{id}/claim")
    @Operation(summary = "Claim task", description = "Claim an unassigned task")
    public ResponseEntity<ApiResponse<Map<String, String>>> claimTask(
            @Parameter(description = "Task ID") @PathVariable String id) {

        taskManagementService.claimTask(id);
        return ResponseEntity.ok(ApiResponse.success(Map.of("message", "Task claimed successfully")));
    }

    @PostMapping("/{id}/complete")
    @Operation(summary = "Complete task", description = "Complete a task with optional variables")
    public ResponseEntity<ApiResponse<Map<String, String>>> completeTask(
            @Parameter(description = "Task ID") @PathVariable String id,
            @RequestBody CompleteTaskRequest request) {

        taskManagementService.completeTask(id, request.getVariables());
        return ResponseEntity.ok(ApiResponse.success(Map.of("message", "Task completed successfully")));
    }

    @PostMapping("/{id}/delegate")
    @Operation(summary = "Delegate task", description = "Delegate a task to another user")
    public ResponseEntity<ApiResponse<Map<String, String>>> delegateTask(
            @Parameter(description = "Task ID") @PathVariable String id,
            @RequestBody DelegateTaskRequest request) {

        taskManagementService.delegateTask(id, request.getDelegateTo());
        return ResponseEntity.ok(ApiResponse.success(Map.of("message", "Task delegated successfully")));
    }

    @PostMapping("/{id}/reassign")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reassign task", description = "Reassign a task to a different user (admin only)")
    public ResponseEntity<ApiResponse<Map<String, String>>> reassignTask(
            @Parameter(description = "Task ID") @PathVariable String id,
            @RequestBody ReassignTaskRequest request) {

        taskManagementService.reassignTask(id, request.getAssignee());
        return ResponseEntity.ok(ApiResponse.success(Map.of("message", "Task reassigned successfully")));
    }

    @PostMapping("/{id}/cc")
    @Operation(summary = "Add CC users", description = "Add carbon copy users to a task")
    public ResponseEntity<ApiResponse<Map<String, String>>> addCcUsers(
            @Parameter(description = "Task ID") @PathVariable String id,
            @RequestBody CcUsersRequest request) {

        taskManagementService.addCcUsers(id, request.getCcUsers());
        return ResponseEntity.ok(ApiResponse.success(Map.of("message", "CC users added successfully")));
    }

    @DeleteMapping("/{id}/cc/{username}")
    @Operation(summary = "Remove CC user", description = "Remove a carbon copy user from a task")
    public ResponseEntity<ApiResponse<Map<String, String>>> removeCcUser(
            @Parameter(description = "Task ID") @PathVariable String id,
            @Parameter(description = "Username to remove") @PathVariable String username) {

        taskManagementService.removeCcUser(id, username);
        return ResponseEntity.ok(ApiResponse.success(Map.of("message", "CC user removed successfully")));
    }

    @GetMapping("/{id}/cc")
    @Operation(summary = "Get CC users", description = "Get all carbon copy users for a task")
    public ResponseEntity<ApiResponse<List<String>>> getCcUsers(
            @Parameter(description = "Task ID") @PathVariable String id) {

        List<String> ccUsers = taskManagementService.getCcUsers(id);
        return ResponseEntity.ok(ApiResponse.success(ccUsers));
    }

    @GetMapping("/expiration-alerts")
    @Operation(summary = "Get expiration alerts", description = "Get tasks with expiration alerts (overdue or due soon)")
    public ResponseEntity<ApiResponse<List<TaskDTO>>> getExpirationAlerts() {
        List<TaskDTO> tasks = taskManagementService.getTasksWithExpirationAlerts();
        return ResponseEntity.ok(ApiResponse.success(tasks));
    }

    // Request DTOs

    public static class CompleteTaskRequest {
        private Map<String, Object> variables;

        public Map<String, Object> getVariables() {
            return variables;
        }

        public void setVariables(Map<String, Object> variables) {
            this.variables = variables;
        }
    }

    public static class DelegateTaskRequest {
        private String delegateTo;

        public String getDelegateTo() {
            return delegateTo;
        }

        public void setDelegateTo(String delegateTo) {
            this.delegateTo = delegateTo;
        }
    }

    public static class ReassignTaskRequest {
        private String assignee;

        public String getAssignee() {
            return assignee;
        }

        public void setAssignee(String assignee) {
            this.assignee = assignee;
        }
    }

    public static class CcUsersRequest {
        private List<String> ccUsers;

        public List<String> getCcUsers() {
            return ccUsers;
        }

        public void setCcUsers(List<String> ccUsers) {
            this.ccUsers = ccUsers;
        }
    }
}
