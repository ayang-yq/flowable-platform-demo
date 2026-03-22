package com.flowable.platform.test.integration.service;

import com.flowable.platform.dto.TaskDTO;
import com.flowable.platform.service.TaskManagementService;
import com.flowable.platform.test.flowable.AbstractFlowableTest;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for TaskManagementService.
 * Tests service layer with real Flowable engine and database.
 */
@DisplayName("TaskManagementService Integration Tests")
class TaskServiceIntegrationTest extends AbstractFlowableTest {

    @Autowired
    private TaskManagementService taskManagementService;

    @Test
    @DisplayName("Should get my tasks")
    void shouldGetMyTasks() {
        // Given - a process with task claimed by test user
        deployProcess("test-simple-process.bpmn20.xml");

        ProcessInstance pi = runtimeService.startProcessInstanceByKey("test-simple-process");

        // Claim the task so getMyTasks finds it
        Task task = taskService.createTaskQuery()
                .processInstanceId(pi.getId())
                .singleResult();
        taskManagementService.claimTask(task.getId());

        // When
        Page<TaskDTO> tasks = taskManagementService.getMyTasks(
                null, null, null, PageRequest.of(0, 10));

        // Then
        assertThat(tasks).isNotNull();
        assertThat(tasks.getContent()).isNotEmpty();
    }

    @Test
    @DisplayName("Should claim unassigned task")
    void shouldClaimUnassignedTask() {
        // Given - process with unassigned task
        deployProcess("test-simple-process.bpmn20.xml");

        ProcessInstance pi = runtimeService.startProcessInstanceByKey("test-simple-process");
        Task unassignedTask = taskService.createTaskQuery()
                .processInstanceId(pi.getId())
                .singleResult();

        assertThat(unassignedTask.getAssignee()).isNull();

        // When
        TaskDTO claimedTask = taskManagementService.claimTask(unassignedTask.getId());

        // Then
        assertThat(claimedTask).isNotNull();
        assertThat(claimedTask.getAssignee()).isEqualTo(getTestUserId());
    }

    @Test
    @DisplayName("Should complete task with variables")
    void shouldCompleteTaskWithVariables() {
        // Given
        deployProcess("test-simple-process.bpmn20.xml");

        ProcessInstance pi = runtimeService.startProcessInstanceByKey("test-simple-process");
        Task task = taskService.createTaskQuery()
                .processInstanceId(pi.getId())
                .singleResult();

        // First claim the task
        taskManagementService.claimTask(task.getId());

        Map<String, Object> variables = Map.of(
                "approved", true,
                "comment", "Test completion comment"
        );

        // When
        taskManagementService.completeTask(task.getId(), variables);

        // Then - task should be completed
        Task completedTask = taskService.createTaskQuery()
                .taskId(task.getId())
                .singleResult();

        assertThat(completedTask).isNull();
    }

    @Test
    @DisplayName("Should throw exception when completing non-existent task")
    void shouldThrowExceptionWhenCompletingNonExistentTask() {
        assertThatThrownBy(() -> taskManagementService.completeTask("non-existent-id", null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Task not found");
    }

    @Test
    @DisplayName("Should delegate task to another user")
    void shouldDelegateTask() {
        // Given
        deployProcess("test-simple-process.bpmn20.xml");

        ProcessInstance pi = runtimeService.startProcessInstanceByKey("test-simple-process");
        Task task = taskService.createTaskQuery()
                .processInstanceId(pi.getId())
                .singleResult();

        // Claim first
        taskManagementService.claimTask(task.getId());

        // When
        taskManagementService.delegateTask(task.getId(), "delegate-user");

        // Then
        Task delegatedTask = taskService.createTaskQuery()
                .taskId(task.getId())
                .singleResult();

        assertThat(delegatedTask.getAssignee()).isEqualTo("delegate-user");
    }

    @Test
    @DisplayName("Should add CC users to task")
    void shouldAddCcUsers() {
        // Given
        deployProcess("test-simple-process.bpmn20.xml");

        ProcessInstance pi = runtimeService.startProcessInstanceByKey("test-simple-process");
        Task task = taskService.createTaskQuery()
                .processInstanceId(pi.getId())
                .singleResult();

        List<String> ccUsers = List.of("user1", "user2");

        // When
        taskManagementService.addCcUsers(task.getId(), ccUsers);

        // Then
        List<String> retrievedCcUsers = taskManagementService.getCcUsers(task.getId());
        assertThat(retrievedCcUsers).containsExactlyInAnyOrder("user1", "user2");
    }

    @Test
    @DisplayName("Should get tasks with expiration alerts")
    void shouldGetTasksWithExpirationAlerts() {
        // Given - tasks with due dates (some overdue, some approaching)
        deployProcess("test-simple-process.bpmn20.xml");

        ProcessInstance pi = runtimeService.startProcessInstanceByKey("test-simple-process");
        Task task = taskService.createTaskQuery()
                .processInstanceId(pi.getId())
                .singleResult();

        // Set due date to 1 business day from now
        taskManagementService.setTaskDueDateWithBusinessCalendar(task.getId(), 1);

        // When
        List<TaskDTO> alerts = taskManagementService.getTasksWithExpirationAlerts();

        // Then
        assertThat(alerts).isNotNull();
    }

    @Test
    @DisplayName("Should check if task is overdue")
    void shouldCheckIfTaskIsOverdue() {
        // Given
        deployProcess("test-simple-process.bpmn20.xml");

        ProcessInstance pi = runtimeService.startProcessInstanceByKey("test-simple-process");
        Task task = taskService.createTaskQuery()
                .processInstanceId(pi.getId())
                .singleResult();

        // Set due date in the past
        taskManagementService.setTaskDueDateWithBusinessCalendar(task.getId(), -5);

        // When
        boolean isOverdue = taskManagementService.isTaskOverdue(task.getId());

        // Then
        assertThat(isOverdue).isTrue();
    }

    // Helper method - in real scenario, get from security context
    private String getTestUserId() {
        return "test-user";
    }
}
