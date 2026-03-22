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
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SimpleProcessTest extends AbstractFlowableTest {

    private String deploymentId;
    private ProcessInstance processInstance;

    @BeforeAll
    void setupProcess() {
        // Deploy the test process before all tests
        deploymentId = deployProcess("test-simple-process.bpmn20.xml");
        assertThat(deploymentId).isNotNull();
    }

    @AfterAll
    void cleanup() {
        // Clean up deployment after all tests
        if (deploymentId != null) {
            deleteDeployment(deploymentId);
        }
    }

    @Test
    @Order(1)
    @DisplayName("Should deploy test process successfully")
    void shouldDeployProcessSuccessfully() {
        // Verify process definition is deployed
        var processDefinition = repositoryService.createProcessDefinitionQuery()
            .processDefinitionKey("test-simple-process")
            .singleResult();

        assertThat(processDefinition).isNotNull();
        assertThat(processDefinition.getKey()).isEqualTo("test-simple-process");
    }

    @Test
    @Order(2)
    @DisplayName("Should start process instance")
    void shouldStartProcessInstance() {
        // Start a new process instance
        processInstance = startProcess("test-simple-process");

        assertThat(processInstance).isNotNull();
        assertThat(processInstance.getId()).isNotNull();
        assertThat(processInstance.getProcessDefinitionKey()).isEqualTo("test-simple-process");
    }

    @Test
    @Order(3)
    @DisplayName("Should create user task after start event")
    void shouldCreateUserTaskAfterStartEvent() {
        // Verify task is created
        Task task = getCurrentTask(processInstance);

        assertThat(task).isNotNull();
        assertThat(task.getName()).isEqualTo("Test User Task");
        assertThat(task.getProcessInstanceId()).isEqualTo(processInstance.getId());
    }

    @Test
    @Order(4)
    @DisplayName("Should complete task and finish process")
    void shouldCompleteTaskAndFinishProcess() {
        // Get the task
        Task task = getCurrentTask(processInstance);
        String taskId = task.getId();

        // Complete the task
        completeTask(taskId);

        // Verify process is completed
        assertProcessCompleted(processInstance.getId());
    }

    @Test
    @Order(5)
    @DisplayName("Should complete process with variables")
    void shouldCompleteProcessWithVariables() {
        // Start a new process instance with variables
        Map<String, Object> variables = Map.of(
            "approved", true,
            "comment", "Test approval"
        );

        ProcessInstance pi = startProcessWithVariables("test-simple-process", variables);

        // Get the task
        Task task = getCurrentTask(pi);
        String taskId = task.getId();

        // Complete the task with additional variables
        completeTaskWithVariables(taskId, Map.of("finalApproval", false));

        // Verify process is completed
        assertProcessCompleted(pi.getId());

        // Verify variables are stored
        Map<String, Object> storedVars = runtimeService.getVariables(pi.getId());
        assertThat(storedVars).containsKey("approved");
        assertThat(storedVars).containsKey("comment");
    }

    @Test
    @Order(6)
    @DisplayName("Should claim task before completing")
    void shouldClaimTaskBeforeCompleting() {
        // Start a new process instance
        ProcessInstance pi = startProcess("test-simple-process");

        // Get the task
        Task task = getCurrentTask(pi);
        String taskId = task.getId();

        // Verify task is unassigned
        assertThat(task.getAssignee()).isNull();

        // Claim the task
        claimTask(taskId, "test-user");

        // Verify task is assigned
        Task claimedTask = taskService.createTaskQuery()
            .taskId(taskId)
            .singleResult();

        assertThat(claimedTask.getAssignee()).isEqualTo("test-user");

        // Complete the task
        completeTask(taskId);

        // Verify process is completed
        assertProcessCompleted(pi.getId());
    }

    @Test
    @Order(7)
    @DisplayName("Should get process variables before completion")
    void shouldGetProcessVariablesBeforeCompletion() {
        // Start a process with known variables
        Map<String, Object> variables = Map.of(
            "employeeName", "John Doe",
            "department", "Engineering"
        );

        ProcessInstance pi = startProcessWithVariables("test-simple-process", variables);

        // Verify variables are accessible
        String employeeName = getProcessVariable(pi.getId(), "employeeName");
        String department = getProcessVariable(pi.getId(), "department");

        assertThat(employeeName).isEqualTo("John Doe");
        assertThat(department).isEqualTo("Engineering");

        // Complete the task to clean up
        Task task = getCurrentTask(pi);
        completeTask(task.getId());
        assertProcessCompleted(pi.getId());
    }

    @Test
    @Order(8)
    @DisplayName("Should query task by assignee")
    void shouldQueryTaskByAssignee() {
        // Start a process
        ProcessInstance pi = startProcess("test-simple-process");

        // Get and claim the task
        Task task = getCurrentTask(pi);
        claimTask(task.getId(), "another-user");

        // Query tasks by assignee
        var tasks = taskService.createTaskQuery()
            .taskAssignee("another-user")
            .list();

        assertThat(tasks).isNotEmpty();
        assertThat(tasks).anyMatch(t -> t.getId().equals(task.getId()));

        // Clean up
        completeTask(task.getId());
        assertProcessCompleted(pi.getId());
    }

    @Test
    @Order(9)
    @DisplayName("Should verify task is in progress before completion")
    void shouldVerifyTaskIsInProgressBeforeCompletion() {
        // Start a process
        ProcessInstance pi = startProcess("test-simple-process");

        // Verify process is running
        assertProcessRunning(pi.getId());

        // Get the task
        Task task = getCurrentTask(pi);
        assertThat(task).isNotNull();

        // Complete the task
        completeTask(task.getId());

        // Verify process is no longer running
        assertProcessCompleted(pi.getId());
    }
}
