package com.flowable.platform.integration;

import com.flowable.platform.config.AbstractIntegrationTest;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TaskServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private org.flowable.engine.TaskService flowableTaskService;

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
    void testGetMyTasks() {
        // Deploy and start process
        repositoryService.createDeployment()
                .addClasspathResource("processes/simple-approval.bpmn20.xml")
                .deploy();

        Map<String, Object> variables = new HashMap<>();
        variables.put("employeeName", "John Doe");
        variables.put("reason", "Test approval");

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("simpleApproval", variables);

        // Get tasks for user
        List<Task> tasks = flowableTaskService.createTaskQuery()
                .taskAssignee("testuser")
                .list();

        assertNotNull(tasks);
        assertFalse(tasks.isEmpty());
    }

    @Test
    @Transactional
    void testClaimTask() {
        // Deploy and start process
        repositoryService.createDeployment()
                .addClasspathResource("processes/simple-approval.bpmn20.xml")
                .deploy();

        Map<String, Object> variables = new HashMap<>();
        variables.put("employeeName", "John Doe");
        variables.put("reason", "Test approval");

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("simpleApproval", variables);

        // Get unassigned task
        List<Task> tasks = flowableTaskService.createTaskQuery()
                .processInstanceId(processInstance.getId())
                .list();

        assertFalse(tasks.isEmpty());
        Task task = tasks.get(0);

        // Claim task
        flowableTaskService.setAssignee(task.getId(), "testuser");

        // Verify claim
        Task claimedTask = flowableTaskService.createTaskQuery()
                .taskId(task.getId())
                .singleResult();

        assertNotNull(claimedTask);
        assertEquals("testuser", claimedTask.getAssignee());
    }

    @Test
    @Transactional
    void testCompleteTask() {
        // Deploy and start process
        repositoryService.createDeployment()
                .addClasspathResource("processes/simple-approval.bpmn20.xml")
                .deploy();

        Map<String, Object> variables = new HashMap<>();
        variables.put("employeeName", "John Doe");
        variables.put("reason", "Test approval");

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("simpleApproval", variables);

        // Get and assign task
        List<Task> tasks = flowableTaskService.createTaskQuery()
                .processInstanceId(processInstance.getId())
                .list();

        assertFalse(tasks.isEmpty());
        Task task = tasks.get(0);
        flowableTaskService.setAssignee(task.getId(), "testuser");

        // Complete task with variables
        Map<String, Object> taskVariables = new HashMap<>();
        taskVariables.put("approved", true);
        taskVariables.put("comment", "Looks good");

        flowableTaskService.complete(task.getId(), taskVariables);

        // Verify task completion
        Task completedTask = flowableTaskService.createTaskQuery()
                .taskId(task.getId())
                .singleResult();

        assertNull(completedTask);

        // Verify variables in history
        Map<String, Object> historyVariables = historyService.createHistoricVariableInstanceQuery()
                .processInstanceId(processInstance.getId())
                .list()
                .stream()
                .collect(HashMap::new, (m, v) -> m.put(v.getVariableName(), v.getValue()), HashMap::putAll);

        assertTrue(historyVariables.containsKey("approved"));
        assertTrue(historyVariables.containsKey("comment"));
    }

    @Test
    @Transactional
    void testDelegateTask() {
        // Deploy and start process
        repositoryService.createDeployment()
                .addClasspathResource("processes/simple-approval.bpmn20.xml")
                .deploy();

        Map<String, Object> variables = new HashMap<>();
        variables.put("employeeName", "John Doe");
        variables.put("reason", "Test approval");

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("simpleApproval", variables);

        // Get and assign task to user1
        List<Task> tasks = flowableTaskService.createTaskQuery()
                .processInstanceId(processInstance.getId())
                .list();

        assertFalse(tasks.isEmpty());
        Task task = tasks.get(0);
        flowableTaskService.setAssignee(task.getId(), "user1");

        // Delegate task to user2
        flowableTaskService.setOwner(task.getId(), "user1");
        flowableTaskService.setAssignee(task.getId(), "user2");

        // Verify delegation
        Task delegatedTask = flowableTaskService.createTaskQuery()
                .taskId(task.getId())
                .singleResult();

        assertNotNull(delegatedTask);
        assertEquals("user2", delegatedTask.getAssignee());
        assertEquals("user1", delegatedTask.getOwner());
    }

    @Test
    @Transactional
    void testGetCompletedTasks() {
        // Deploy and start process
        repositoryService.createDeployment()
                .addClasspathResource("processes/simple-approval.bpmn20.xml")
                .deploy();

        Map<String, Object> variables = new HashMap<>();
        variables.put("employeeName", "John Doe");
        variables.put("reason", "Test approval");

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("simpleApproval", variables);

        // Get and complete task
        List<Task> tasks = flowableTaskService.createTaskQuery()
                .processInstanceId(processInstance.getId())
                .list();

        assertFalse(tasks.isEmpty());
        Task task = tasks.get(0);
        String taskId = task.getId();

        Map<String, Object> taskVariables = new HashMap<>();
        taskVariables.put("approved", true);
        flowableTaskService.complete(taskId, taskVariables);

        // Verify in history
        long completedCount = historyService.createHistoricTaskInstanceQuery()
                .taskId(taskId)
                .finished()
                .count();

        assertEquals(1, completedCount);
    }

    @Test
    @Transactional
    void testTaskFilteringByPriority() {
        // Deploy and start process
        repositoryService.createDeployment()
                .addClasspathResource("processes/simple-approval.bpmn20.xml")
                .deploy();

        Map<String, Object> variables = new HashMap<>();
        variables.put("employeeName", "John Doe");
        variables.put("reason", "Test approval");

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("simpleApproval", variables);

        // Get task and set priority
        List<Task> tasks = flowableTaskService.createTaskQuery()
                .processInstanceId(processInstance.getId())
                .list();

        assertFalse(tasks.isEmpty());
        Task task = tasks.get(0);
        flowableTaskService.setPriority(task.getId(), 80);

        // Query by priority
        List<Task> highPriorityTasks = flowableTaskService.createTaskQuery()
                .taskMinPriority(50)
                .list();

        assertFalse(highPriorityTasks.isEmpty());
        assertTrue(highPriorityTasks.stream().anyMatch(t -> t.getId().equals(task.getId())));
    }

    @Test
    @Transactional
    void testTaskDueDate() {
        // Deploy and start process
        repositoryService.createDeployment()
                .addClasspathResource("processes/simple-approval.bpmn20.xml")
                .deploy();

        Map<String, Object> variables = new HashMap<>();
        variables.put("employeeName", "John Doe");
        variables.put("reason", "Test approval");

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("simpleApproval", variables);

        // Get task and set due date
        List<Task> tasks = flowableTaskService.createTaskQuery()
                .processInstanceId(processInstance.getId())
                .list();

        assertFalse(tasks.isEmpty());
        Task task = tasks.get(0);

        LocalDateTime dueDate = LocalDateTime.now().plusDays(2);
        flowableTaskService.setDueDate(task.getId(), java.sql.Timestamp.valueOf(dueDate));

        // Verify due date
        Task taskWithDueDate = flowableTaskService.createTaskQuery()
                .taskId(task.getId())
                .singleResult();

        assertNotNull(taskWithDueDate.getDueDate());
    }

    @Test
    @Transactional
    void testTaskExpiration() {
        // Deploy and start process
        repositoryService.createDeployment()
                .addClasspathResource("processes/simple-approval.bpmn20.xml")
                .deploy();

        Map<String, Object> variables = new HashMap<>();
        variables.put("employeeName", "John Doe");
        variables.put("reason", "Test approval");

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("simpleApproval", variables);

        // Get task and set past due date
        List<Task> tasks = flowableTaskService.createTaskQuery()
                .processInstanceId(processInstance.getId())
                .list();

        assertFalse(tasks.isEmpty());
        Task task = tasks.get(0);

        LocalDateTime pastDueDate = LocalDateTime.now().minusDays(1);
        flowableTaskService.setDueDate(task.getId(), java.sql.Timestamp.valueOf(pastDueDate));

        // Verify due date was set
        Task taskWithDueDate = flowableTaskService.createTaskQuery()
                .taskId(task.getId())
                .singleResult();

        assertNotNull(taskWithDueDate.getDueDate());
        assertTrue(taskWithDueDate.getDueDate().before(java.sql.Timestamp.valueOf(LocalDateTime.now())));
    }

    @Test
    @Transactional
    void testTaskCcUsers() {
        // Deploy and start process
        repositoryService.createDeployment()
                .addClasspathResource("processes/simple-approval.bpmn20.xml")
                .deploy();

        Map<String, Object> variables = new HashMap<>();
        variables.put("employeeName", "John Doe");
        variables.put("reason", "Test approval");

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("simpleApproval", variables);

        // Get task
        List<Task> tasks = flowableTaskService.createTaskQuery()
                .processInstanceId(processInstance.getId())
                .list();

        assertFalse(tasks.isEmpty());
        Task task = tasks.get(0);

        // Add CC users as a task variable
        List<String> ccUsers = List.of("user1", "user2");
        flowableTaskService.setVariable(task.getId(), "ccUsers", ccUsers);

        // Verify CC users
        @SuppressWarnings("unchecked")
        List<String> retrievedCcUsers = (List<String>) flowableTaskService.getVariable(task.getId(), "ccUsers");

        assertNotNull(retrievedCcUsers);
        assertEquals(2, retrievedCcUsers.size());
        assertTrue(retrievedCcUsers.contains("user1"));
        assertTrue(retrievedCcUsers.contains("user2"));
    }

    @Test
    @Transactional
    void testAddRemoveCcUsers() {
        // Deploy and start process
        repositoryService.createDeployment()
                .addClasspathResource("processes/simple-approval.bpmn20.xml")
                .deploy();

        Map<String, Object> variables = new HashMap<>();
        variables.put("employeeName", "John Doe");
        variables.put("reason", "Test approval");

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("simpleApproval", variables);

        // Get task
        List<Task> tasks = flowableTaskService.createTaskQuery()
                .processInstanceId(processInstance.getId())
                .list();

        assertFalse(tasks.isEmpty());
        Task task = tasks.get(0);

        // Add initial CC users
        List<String> ccUsers = new java.util.ArrayList<>(List.of("user1", "user2"));
        flowableTaskService.setVariable(task.getId(), "ccUsers", ccUsers);

        // Add another CC user
        ccUsers.add("user3");
        flowableTaskService.setVariable(task.getId(), "ccUsers", ccUsers);

        // Verify
        @SuppressWarnings("unchecked")
        List<String> retrievedCcUsers = (List<String>) flowableTaskService.getVariable(task.getId(), "ccUsers");
        assertEquals(3, retrievedCcUsers.size());

        // Remove a CC user
        ccUsers.remove("user2");
        flowableTaskService.setVariable(task.getId(), "ccUsers", ccUsers);

        // Verify removal
        @SuppressWarnings("unchecked")
        List<String> finalCcUsers = (List<String>) flowableTaskService.getVariable(task.getId(), "ccUsers");
        assertEquals(2, finalCcUsers.size());
        assertFalse(finalCcUsers.contains("user2"));
    }
}
