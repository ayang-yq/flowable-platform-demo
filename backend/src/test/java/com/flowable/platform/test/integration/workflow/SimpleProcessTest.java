package com.flowable.platform.test.integration.workflow;

import com.flowable.platform.test.flowable.AbstractFlowableTest;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.junit.jupiter.api.*;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for simple Flowable process execution.
 * Tests process deployment, task retrieval, completion, and state validation.
 */
@DisplayName("Simple Process Integration Tests")
class SimpleProcessTest extends AbstractFlowableTest {

    @Test
    @DisplayName("Should deploy test process successfully")
    void shouldDeployProcessSuccessfully() {
        deployProcess("test-simple-process.bpmn20.xml");

        var processDefinition = repositoryService.createProcessDefinitionQuery()
            .processDefinitionKey("test-simple-process")
            .latestVersion()
            .singleResult();

        assertThat(processDefinition).isNotNull();
        assertThat(processDefinition.getKey()).isEqualTo("test-simple-process");
    }

    @Test
    @DisplayName("Should start process instance")
    void shouldStartProcessInstance() {
        deployProcess("test-simple-process.bpmn20.xml");

        ProcessInstance processInstance = startProcess("test-simple-process");

        assertThat(processInstance).isNotNull();
        assertThat(processInstance.getId()).isNotNull();
        assertThat(processInstance.getProcessDefinitionKey()).isEqualTo("test-simple-process");
    }

    @Test
    @DisplayName("Should create user task after start event")
    void shouldCreateUserTaskAfterStartEvent() {
        deployProcess("test-simple-process.bpmn20.xml");
        ProcessInstance processInstance = startProcess("test-simple-process");

        Task task = getCurrentTask(processInstance);

        assertThat(task).isNotNull();
        assertThat(task.getName()).isEqualTo("Test User Task");
        assertThat(task.getProcessInstanceId()).isEqualTo(processInstance.getId());
    }

    @Test
    @DisplayName("Should complete task and finish process")
    void shouldCompleteTaskAndFinishProcess() {
        deployProcess("test-simple-process.bpmn20.xml");
        ProcessInstance processInstance = startProcess("test-simple-process");

        Task task = getCurrentTask(processInstance);
        String taskId = task.getId();

        completeTask(taskId);

        assertProcessCompleted(processInstance.getId());
    }

    @Test
    @DisplayName("Should complete process with variables")
    void shouldCompleteProcessWithVariables() {
        deployProcess("test-simple-process.bpmn20.xml");

        Map<String, Object> variables = Map.of(
            "approved", true,
            "comment", "Test approval"
        );

        ProcessInstance pi = startProcessWithVariables("test-simple-process", variables);

        // Verify variables are stored before completing
        Map<String, Object> storedVars = runtimeService.getVariables(pi.getId());
        assertThat(storedVars).containsKey("approved");
        assertThat(storedVars).containsKey("comment");

        // Complete the task
        Task task = getCurrentTask(pi);
        completeTaskWithVariables(task.getId(), Map.of("finalApproval", false));

        assertProcessCompleted(pi.getId());
    }

    @Test
    @DisplayName("Should claim task before completing")
    void shouldClaimTaskBeforeCompleting() {
        deployProcess("test-simple-process.bpmn20.xml");
        ProcessInstance pi = startProcess("test-simple-process");

        Task task = getCurrentTask(pi);
        String taskId = task.getId();

        assertThat(task.getAssignee()).isNull();

        claimTask(taskId, "test-user");

        Task claimedTask = taskService.createTaskQuery()
            .taskId(taskId)
            .singleResult();

        assertThat(claimedTask.getAssignee()).isEqualTo("test-user");

        completeTask(taskId);

        assertProcessCompleted(pi.getId());
    }

    @Test
    @DisplayName("Should get process variables before completion")
    void shouldGetProcessVariablesBeforeCompletion() {
        deployProcess("test-simple-process.bpmn20.xml");

        Map<String, Object> variables = Map.of(
            "employeeName", "John Doe",
            "department", "Engineering"
        );

        ProcessInstance pi = startProcessWithVariables("test-simple-process", variables);

        String employeeName = getProcessVariable(pi.getId(), "employeeName");
        String department = getProcessVariable(pi.getId(), "department");

        assertThat(employeeName).isEqualTo("John Doe");
        assertThat(department).isEqualTo("Engineering");

        Task task = getCurrentTask(pi);
        completeTask(task.getId());
        assertProcessCompleted(pi.getId());
    }

    @Test
    @DisplayName("Should query task by assignee")
    void shouldQueryTaskByAssignee() {
        deployProcess("test-simple-process.bpmn20.xml");
        ProcessInstance pi = startProcess("test-simple-process");

        Task task = getCurrentTask(pi);
        claimTask(task.getId(), "another-user");

        var tasks = taskService.createTaskQuery()
            .taskAssignee("another-user")
            .list();

        assertThat(tasks).isNotEmpty();
        assertThat(tasks).anyMatch(t -> t.getId().equals(task.getId()));

        completeTask(task.getId());
        assertProcessCompleted(pi.getId());
    }

    @Test
    @DisplayName("Should verify task is in progress before completion")
    void shouldVerifyTaskIsInProgressBeforeCompletion() {
        deployProcess("test-simple-process.bpmn20.xml");
        ProcessInstance pi = startProcess("test-simple-process");

        assertProcessRunning(pi.getId());

        Task task = getCurrentTask(pi);
        assertThat(task).isNotNull();

        completeTask(task.getId());

        assertProcessCompleted(pi.getId());
    }
}
