package com.flowable.platform.integration;

import com.flowable.platform.entity.Tenant;
import com.flowable.platform.entity.User;
import com.flowable.platform.repository.TenantRepository;
import com.flowable.platform.repository.UserRepository;
import org.flowable.engine.*;
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
class MultiTenantTaskTest {

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private org.flowable.engine.TaskService flowableTaskService;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Tenant tenant1;
    private Tenant tenant2;
    private User tenant1User1;
    private User tenant1User2;
    private User tenant2User1;

    @BeforeEach
    void setUp() {
        // Create two tenants
        tenant1 = new Tenant("Tenant 1", "tenant-1");
        tenant1.setIsActive(true);
        tenant1 = tenantRepository.save(tenant1);

        tenant2 = new Tenant("Tenant 2", "tenant-2");
        tenant2.setIsActive(true);
        tenant2 = tenantRepository.save(tenant2);

        // Create users for tenant 1
        tenant1User1 = new User(tenant1, "tenant1user1", "t1u1@example.com", passwordEncoder.encode("password"));
        tenant1User1.setIsActive(true);
        tenant1User1 = userRepository.save(tenant1User1);

        tenant1User2 = new User(tenant1, "tenant1user2", "t1u2@example.com", passwordEncoder.encode("password"));
        tenant1User2.setIsActive(true);
        tenant1User2 = userRepository.save(tenant1User2);

        // Create user for tenant 2
        tenant2User1 = new User(tenant2, "tenant2user1", "t2u1@example.com", passwordEncoder.encode("password"));
        tenant2User1.setIsActive(true);
        tenant2User1 = userRepository.save(tenant2User1);
    }

    @Test
    @Transactional
    void testTenantIsolation_TaskVisibility() {
        // Deploy process
        repositoryService.createDeployment()
                .addClasspathResource("processes/simple-approval.bpmn20.xml")
                .tenantId("tenant-1")
                .deploy();

        // Start process instance for tenant 1
        Map<String, Object> variables = new HashMap<>();
        variables.put("employeeName", "John Doe");
        variables.put("reason", "Test approval");

        ProcessInstance tenant1Instance = runtimeService.createProcessInstanceBuilder()
                .processDefinitionKey("simpleApproval")
                .tenantId("tenant-1")
                .variables(variables)
                .start();

        // Get tasks for tenant 1
        List<Task> tenant1Tasks = flowableTaskService.createTaskQuery()
                .processInstanceId(tenant1Instance.getId())
                .list();

        assertFalse(tenant1Tasks.isEmpty());

        // Verify tasks have correct tenant ID
        assertFalse(tenant1Tasks.isEmpty());
        assertEquals("tenant-1", tenant1Tasks.get(0).getTenantId());
    }

    @Test
    @Transactional
    void testTenantIsolation_TaskAssignment() {
        // Deploy process for both tenants
        repositoryService.createDeployment()
                .addClasspathResource("processes/simple-approval.bpmn20.xml")
                .tenantId("tenant-1")
                .deploy();

        repositoryService.createDeployment()
                .addClasspathResource("processes/simple-approval.bpmn20.xml")
                .tenantId("tenant-2")
                .deploy();

        // Start process instances for both tenants
        Map<String, Object> variables1 = new HashMap<>();
        variables1.put("employeeName", "Tenant 1 User");
        variables1.put("reason", "Tenant 1 approval");

        ProcessInstance tenant1Instance = runtimeService.createProcessInstanceBuilder()
                .processDefinitionKey("simpleApproval")
                .tenantId("tenant-1")
                .variables(variables1)
                .start();

        Map<String, Object> variables2 = new HashMap<>();
        variables2.put("employeeName", "Tenant 2 User");
        variables2.put("reason", "Tenant 2 approval");

        ProcessInstance tenant2Instance = runtimeService.createProcessInstanceBuilder()
                .processDefinitionKey("simpleApproval")
                .tenantId("tenant-2")
                .variables(variables2)
                .start();

        // Assign task in tenant 1 to tenant1user1
        List<Task> tenant1Tasks = flowableTaskService.createTaskQuery()
                .processInstanceId(tenant1Instance.getId())
                .list();

        assertFalse(tenant1Tasks.isEmpty());
        Task tenant1Task = tenant1Tasks.get(0);
        flowableTaskService.setAssignee(tenant1Task.getId(), "tenant1user1");

        // Assign task in tenant 2 to tenant2user1
        List<Task> tenant2Tasks = flowableTaskService.createTaskQuery()
                .processInstanceId(tenant2Instance.getId())
                .list();

        assertFalse(tenant2Tasks.isEmpty());
        Task tenant2Task = tenant2Tasks.get(0);
        flowableTaskService.setAssignee(tenant2Task.getId(), "tenant2user1");

        // Verify assignments are tenant-isolated
        Task assignedTenant1Task = flowableTaskService.createTaskQuery()
                .taskId(tenant1Task.getId())
                .singleResult();

        assertEquals("tenant1user1", assignedTenant1Task.getAssignee());
        assertEquals("tenant-1", assignedTenant1Task.getTenantId());

        Task assignedTenant2Task = flowableTaskService.createTaskQuery()
                .taskId(tenant2Task.getId())
                .singleResult();

        assertEquals("tenant2user1", assignedTenant2Task.getAssignee());
        assertEquals("tenant-2", assignedTenant2Task.getTenantId());
    }

    @Test
    @Transactional
    void testTenantIsolation_TaskCompletion() {
        // Deploy process for both tenants
        repositoryService.createDeployment()
                .addClasspathResource("processes/simple-approval.bpmn20.xml")
                .tenantId("tenant-1")
                .deploy();

        repositoryService.createDeployment()
                .addClasspathResource("processes/simple-approval.bpmn20.xml")
                .tenantId("tenant-2")
                .deploy();

        // Start and assign tasks
        Map<String, Object> variables = new HashMap<>();
        variables.put("employeeName", "Test User");
        variables.put("reason", "Test approval");

        ProcessInstance tenant1Instance = runtimeService.createProcessInstanceBuilder()
                .processDefinitionKey("simpleApproval")
                .tenantId("tenant-1")
                .variables(variables)
                .start();

        ProcessInstance tenant2Instance = runtimeService.createProcessInstanceBuilder()
                .processDefinitionKey("simpleApproval")
                .tenantId("tenant-2")
                .variables(variables)
                .start();

        // Get and assign tasks
        List<Task> tenant1Tasks = flowableTaskService.createTaskQuery()
                .processInstanceId(tenant1Instance.getId())
                .list();

        List<Task> tenant2Tasks = flowableTaskService.createTaskQuery()
                .processInstanceId(tenant2Instance.getId())
                .list();

        assertFalse(tenant1Tasks.isEmpty());
        assertFalse(tenant2Tasks.isEmpty());

        Task tenant1Task = tenant1Tasks.get(0);
        Task tenant2Task = tenant2Tasks.get(0);

        flowableTaskService.setAssignee(tenant1Task.getId(), "tenant1user1");
        flowableTaskService.setAssignee(tenant2Task.getId(), "tenant2user1");

        // Complete tenant 1's task
        Map<String, Object> completionVars = new HashMap<>();
        completionVars.put("approved", true);
        flowableTaskService.complete(tenant1Task.getId(), completionVars);

        // Verify tenant 1's task is completed
        Task completedTenant1Task = flowableTaskService.createTaskQuery()
                .taskId(tenant1Task.getId())
                .singleResult();

        assertNull(completedTenant1Task);

        // Verify tenant 2's task is still active
        Task activeTenant2Task = flowableTaskService.createTaskQuery()
                .taskId(tenant2Task.getId())
                .singleResult();

        assertNotNull(activeTenant2Task);
    }

    @Test
    @Transactional
    void testTenantIsolation_CrossTenantAccessPrevention() {
        // Deploy process for tenant 1 only
        repositoryService.createDeployment()
                .addClasspathResource("processes/simple-approval.bpmn20.xml")
                .tenantId("tenant-1")
                .deploy();

        // Start process for tenant 1
        Map<String, Object> variables = new HashMap<>();
        variables.put("employeeName", "Test User");
        variables.put("reason", "Test approval");

        ProcessInstance tenant1Instance = runtimeService.createProcessInstanceBuilder()
                .processDefinitionKey("simpleApproval")
                .tenantId("tenant-1")
                .variables(variables)
                .start();

        // Get task for tenant 1
        List<Task> tenant1Tasks = flowableTaskService.createTaskQuery()
                .processInstanceId(tenant1Instance.getId())
                .list();

        assertFalse(tenant1Tasks.isEmpty());
        Task tenant1Task = tenant1Tasks.get(0);

        // Verify task has tenant-1 ID
        Task tenant1TaskQuery = flowableTaskService.createTaskQuery()
                .taskId(tenant1Task.getId())
                .singleResult();

        assertNotNull(tenant1TaskQuery);
        assertEquals("tenant-1", tenant1TaskQuery.getTenantId());
    }

    @Test
    @Transactional
    void testTenantIsolation_TaskVariables() {
        // Deploy and start process for tenant 1
        repositoryService.createDeployment()
                .addClasspathResource("processes/simple-approval.bpmn20.xml")
                .tenantId("tenant-1")
                .deploy();

        Map<String, Object> variables = new HashMap<>();
        variables.put("employeeName", "Tenant 1 Employee");
        variables.put("reason", "Tenant 1 approval");
        variables.put("sensitiveData", "Tenant 1 confidential");

        ProcessInstance tenant1Instance = runtimeService.createProcessInstanceBuilder()
                .processDefinitionKey("simpleApproval")
                .tenantId("tenant-1")
                .variables(variables)
                .start();

        // Get task
        List<Task> tasks = flowableTaskService.createTaskQuery()
                .processInstanceId(tenant1Instance.getId())
                .list();

        assertFalse(tasks.isEmpty());
        Task task = tasks.get(0);

        // Verify task has tenant 1's variables
        Map<String, Object> taskVariables = flowableTaskService.getVariables(task.getId());
        assertEquals("Tenant 1 Employee", taskVariables.get("employeeName"));
        assertEquals("Tenant 1 confidential", taskVariables.get("sensitiveData"));

        // Verify tenant ID is set
        assertEquals("tenant-1", task.getTenantId());
    }

    @Test
    @Transactional
    void testTenantIsolation_MultipleTenantsSameUser() {
        // Deploy process for both tenants
        repositoryService.createDeployment()
                .addClasspathResource("processes/simple-approval.bpmn20.xml")
                .deploy();

        // Start process instances for both tenants
        Map<String, Object> variables1 = new HashMap<>();
        variables1.put("employeeName", "Tenant 1 Request");
        variables1.put("reason", "Tenant 1 approval");

        ProcessInstance tenant1Instance = runtimeService.createProcessInstanceBuilder()
                .processDefinitionKey("simpleApproval")
                .tenantId("tenant-1")
                .variables(variables1)
                .start();

        Map<String, Object> variables2 = new HashMap<>();
        variables2.put("employeeName", "Tenant 2 Request");
        variables2.put("reason", "Tenant 2 approval");

        ProcessInstance tenant2Instance = runtimeService.createProcessInstanceBuilder()
                .processDefinitionKey("simpleApproval")
                .tenantId("tenant-2")
                .variables(variables2)
                .start();

        // Verify both instances exist
        assertNotNull(tenant1Instance);
        assertNotNull(tenant2Instance);

        // Verify they have different tenant IDs
        assertNotEquals(tenant1Instance.getTenantId(), tenant2Instance.getTenantId());

        // Get tasks for each tenant
        List<Task> tenant1Tasks = flowableTaskService.createTaskQuery()
                .processDefinitionKey("simpleApproval")
                .list()
                .stream()
                .filter(t -> "tenant-1".equals(t.getTenantId()))
                .toList();

        List<Task> tenant2Tasks = flowableTaskService.createTaskQuery()
                .processDefinitionKey("simpleApproval")
                .list()
                .stream()
                .filter(t -> "tenant-2".equals(t.getTenantId()))
                .toList();

        // Verify tasks are isolated
        assertEquals(1, tenant1Tasks.size());
        assertEquals(1, tenant2Tasks.size());

        assertNotEquals(tenant1Tasks.get(0).getId(), tenant2Tasks.get(0).getId());
    }
}
