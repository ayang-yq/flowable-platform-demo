package com.flowable.platform.test.flowable;

import com.flowable.platform.test.integration.AbstractIntegrationTest;
import com.flowable.platform.test.util.TestTenantContext;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Base class for Flowable process and task integration tests.
 * Provides helpers for process deployment, task completion, and assertions.
 */
@Transactional
public abstract class AbstractFlowableTest extends AbstractIntegrationTest {

    @Autowired
    protected RuntimeService runtimeService;

    @Autowired
    protected TaskService taskService;

    @Autowired
    protected RepositoryService repositoryService;

    @Autowired
    protected HistoryService historyService;

    @Autowired
    protected TestTenantContext testTenantContext;

    @BeforeEach
    void setUpFlowableTest() {
        testTenantContext.setTestTenant();
    }

    /**
     * Deploy test process definition from classpath
     */
    protected String deployProcess(String processResource) {
        return repositoryService.createDeployment()
            .addClasspathResource("processes/" + processResource)
            .deploy()
            .getId();
    }

    /**
     * Start a process instance by key
     */
    protected ProcessInstance startProcess(String processDefinitionKey) {
        return runtimeService.startProcessInstanceByKey(processDefinitionKey);
    }

    /**
     * Start a process instance with variables
     */
    protected ProcessInstance startProcessWithVariables(String processDefinitionKey, Map<String, Object> variables) {
        return runtimeService.startProcessInstanceByKey(processDefinitionKey, variables);
    }

    /**
     * Complete a task by task ID
     */
    protected void completeTask(String taskId) {
        taskService.complete(taskId);
    }

    /**
     * Complete a task with variables
     */
    protected void completeTaskWithVariables(String taskId, Map<String, Object> variables) {
        taskService.complete(taskId, variables);
    }

    /**
     * Get current task for process instance
     */
    protected Task getCurrentTask(ProcessInstance processInstance) {
        return taskService.createTaskQuery()
            .processInstanceId(processInstance.getId())
            .singleResult();
    }

    /**
     * Get all tasks for process instance
     */
    protected java.util.List<Task> getTasks(ProcessInstance processInstance) {
        return taskService.createTaskQuery()
            .processInstanceId(processInstance.getId())
            .list();
    }

    /**
     * Assert process instance is completed
     */
    protected void assertProcessCompleted(String processInstanceId) {
        var hpi = historyService.createHistoricProcessInstanceQuery()
            .processInstanceId(processInstanceId)
            .singleResult();

        assertThat(hpi).isNotNull();
        assertThat(hpi.getEndActivityId()).isNotNull();
    }

    /**
     * Assert process instance is still running
     */
    protected void assertProcessRunning(String processInstanceId) {
        ProcessInstance pi = runtimeService.createProcessInstanceQuery()
            .processInstanceId(processInstanceId)
            .singleResult();

        assertThat(pi).isNotNull();
    }

    /**
     * Assert task exists with given criteria
     */
    protected void assertTaskExists(String taskName) {
        Task task = taskService.createTaskQuery()
            .taskName(taskName)
            .singleResult();

        assertThat(task).isNotNull();
    }

    /**
     * Claim a task for assignee
     */
    protected void claimTask(String taskId, String assignee) {
        taskService.setAssignee(taskId, assignee);
    }

    /**
     * Get task variable
     */
    @SuppressWarnings("unchecked")
    protected <T> T getTaskVariable(String taskId, String variableName) {
        return (T) taskService.getVariable(taskId, variableName);
    }

    /**
     * Set task variable
     */
    protected void setTaskVariable(String taskId, String variableName, Object value) {
        taskService.setVariable(taskId, variableName, value);
    }

    /**
     * Get process variable
     */
    @SuppressWarnings("unchecked")
    protected <T> T getProcessVariable(String processInstanceId, String variableName) {
        return (T) runtimeService.getVariable(processInstanceId, variableName);
    }

    /**
     * Clean up deployment after test
     */
    protected void deleteDeployment(String deploymentId) {
        repositoryService.deleteDeployment(deploymentId, true);
    }
}
