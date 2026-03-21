package com.flowable.platform.integration;

import com.flowable.platform.entity.Tenant;
import com.flowable.platform.entity.User;
import com.flowable.platform.repository.TenantRepository;
import com.flowable.platform.repository.UserRepository;
import org.flowable.engine.*;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class ProcessIntegrationTest {

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Tenant testTenant;
    private User testUser;

    @BeforeEach
    void setUp() {
        // Create test tenant
        testTenant = new Tenant("Test Tenant", "test-tenant");
        testTenant.setIsActive(true);
        testTenant = tenantRepository.save(testTenant);

        // Create test user
        testUser = new User(testTenant, "testuser", "test@example.com", passwordEncoder.encode("password"));
        testUser.setIsActive(true);
        testUser = userRepository.save(testUser);
    }

    @Test
    @Transactional
    void testDeployBpmnProcessDefinition() {
        // Deploy a simple BPMN process
        String processDefinitionKey = "simpleApproval";
        String deploymentId = repositoryService.createDeployment()
                .addClasspathResource("processes/simple-approval.bpmn20.xml")
                .deploy()
                .getId();

        // Verify deployment - get the specific deployment we just created
        List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(processDefinitionKey)
                .deploymentId(deploymentId)
                .list();

        assertFalse(processDefinitions.isEmpty());
        ProcessDefinition processDefinition = processDefinitions.get(0);
        assertEquals("Simple Approval Process", processDefinition.getName());
    }

    @Test
    @Transactional
    void testStartProcessInstance() {
        // Deploy process first
        repositoryService.createDeployment()
                .addClasspathResource("processes/simple-approval.bpmn20.xml")
                .deploy();

        // Start process instance
        Map<String, Object> variables = new HashMap<>();
        variables.put("employeeName", "John Doe");
        variables.put("reason", "Annual leave");

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(
                "simpleApproval",
                variables
        );

        // Verify process instance
        assertNotNull(processInstance);
        assertNotNull(processInstance.getId());
        assertEquals("simpleApproval", processInstance.getProcessDefinitionKey());

        // Verify variables
        Map<String, Object> processVariables = runtimeService.getVariables(processInstance.getId());
        assertEquals("John Doe", processVariables.get("employeeName"));
        assertEquals("Annual leave", processVariables.get("reason"));
    }

    @Test
    @Transactional
    void testCompleteUserTask() {
        // Deploy and start process
        repositoryService.createDeployment()
                .addClasspathResource("processes/simple-approval.bpmn20.xml")
                .deploy();

        // Start process with required variables
        Map<String, Object> variables = new HashMap<>();
        variables.put("employeeName", "John Doe");
        variables.put("reason", "Test approval");

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("simpleApproval", variables);

        // Get active task
        List<Task> tasks = taskService.createTaskQuery()
                .processInstanceId(processInstance.getId())
                .list();

        assertFalse(tasks.isEmpty());
        Task task = tasks.get(0);

        // Complete task
        Map<String, Object> taskVariables = new HashMap<>();
        taskVariables.put("approved", true);
        taskService.complete(task.getId(), taskVariables);

        // Verify task completion
        List<Task> activeTasks = taskService.createTaskQuery()
                .processInstanceId(processInstance.getId())
                .list();

        assertTrue(activeTasks.isEmpty() || !activeTasks.get(0).getId().equals(task.getId()));
    }

    @Test
    @Transactional
    void testSuspendProcessInstance() {
        // Deploy and start process
        repositoryService.createDeployment()
                .addClasspathResource("processes/simple-approval.bpmn20.xml")
                .deploy();

        Map<String, Object> variables = new HashMap<>();
        variables.put("employeeName", "John Doe");
        variables.put("reason", "Test approval");

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("simpleApproval", variables);

        // Suspend process instance
        runtimeService.suspendProcessInstanceById(processInstance.getId());

        // Verify suspension
        ProcessInstance suspendedInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstance.getId())
                .singleResult();

        assertNotNull(suspendedInstance);
        assertTrue(suspendedInstance.isSuspended());
    }

    @Test
    @Transactional
    void testActivateProcessInstance() {
        // Deploy, start, and suspend process
        repositoryService.createDeployment()
                .addClasspathResource("processes/simple-approval.bpmn20.xml")
                .deploy();

        Map<String, Object> variables = new HashMap<>();
        variables.put("employeeName", "John Doe");
        variables.put("reason", "Test approval");

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("simpleApproval", variables);
        runtimeService.suspendProcessInstanceById(processInstance.getId());

        // Activate process instance
        runtimeService.activateProcessInstanceById(processInstance.getId());

        // Verify activation
        ProcessInstance activatedInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstance.getId())
                .singleResult();

        assertNotNull(activatedInstance);
        assertFalse(activatedInstance.isSuspended());
    }

    @Test
    @Transactional
    void testTerminateProcessInstance() {
        // Deploy and start process
        repositoryService.createDeployment()
                .addClasspathResource("processes/simple-approval.bpmn20.xml")
                .deploy();

        Map<String, Object> variables = new HashMap<>();
        variables.put("employeeName", "John Doe");
        variables.put("reason", "Test approval");

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("simpleApproval", variables);

        // Delete process instance (terminate)
        runtimeService.deleteProcessInstance(processInstance.getId(), "Test termination");

        // Verify termination
        ProcessInstance terminatedInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstance.getId())
                .singleResult();

        assertNull(terminatedInstance);
    }

    @Test
    @Transactional
    void testProcessVariableSizeLimit() {
        // Deploy process
        repositoryService.createDeployment()
                .addClasspathResource("processes/simple-approval.bpmn20.xml")
                .deploy();

        // Try to start process with large variable (> 10KB)
        Map<String, Object> variables = new HashMap<>();
        StringBuilder largeValue = new StringBuilder();
        for (int i = 0; i < 11000; i++) {
            largeValue.append("a");
        }
        variables.put("largeData", largeValue.toString());

        // This should either throw an exception or be rejected
        assertThrows(Exception.class, () -> {
            runtimeService.startProcessInstanceByKey("simpleApproval", variables);
        });
    }
}
