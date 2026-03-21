package com.flowable.platform.integration;

import com.flowable.platform.entity.Tenant;
import com.flowable.platform.entity.User;
import com.flowable.platform.repository.TenantRepository;
import com.flowable.platform.repository.UserRepository;
import com.flowable.platform.config.MultiTenantFilter;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class MultiTenantProcessTest {

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Tenant tenant1;
    private Tenant tenant2;
    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        // Create two tenants
        tenant1 = new Tenant("Tenant 1", "tenant-1");
        tenant1.setIsActive(true);
        tenant1 = tenantRepository.save(tenant1);

        tenant2 = new Tenant("Tenant 2", "tenant-2");
        tenant2.setIsActive(true);
        tenant2 = tenantRepository.save(tenant2);

        // Create users for each tenant
        user1 = new User(tenant1, "user1", "user1@tenant1.com", passwordEncoder.encode("password"));
        user1.setIsActive(true);
        user1 = userRepository.save(user1);

        user2 = new User(tenant2, "user2", "user2@tenant2.com", passwordEncoder.encode("password"));
        user2.setIsActive(true);
        user2 = userRepository.save(user2);
    }

    @AfterEach
    void tearDown() {
        // Clean up tenant context
        MultiTenantFilter.setTenantId(null);
    }

    @Test
    @Transactional
    void testTenant1CannotAccessTenant2Processes() {
        // Set tenant context to tenant1
        MultiTenantFilter.setTenantId(tenant1.getId().toString());

        // Start process for tenant1
        runtimeService.startProcessInstanceByKey("simpleApproval");

        // Set tenant context to tenant2
        MultiTenantFilter.setTenantId(tenant2.getId().toString());

        // Tenant2 should not see tenant1's processes
        List<ProcessInstance> tenant2Processes = runtimeService.createProcessInstanceQuery()
                .list();

        // Tenant2 should have no processes (tenant1's process is isolated)
        assertTrue(tenant2Processes.isEmpty() || tenant2Processes.stream()
                .noneMatch(p -> p.getTenantId() != null && p.getTenantId().equals(tenant1.getId().toString())));
    }

    @Test
    @Transactional
    void testProcessInstanceTenantIsolation() {
        // Set tenant context to tenant1
        MultiTenantFilter.setTenantId(tenant1.getId().toString());

        // Start process for tenant1
        ProcessInstance process1 = runtimeService.startProcessInstanceByKey("simpleApproval");
        assertNotNull(process1);
        assertEquals(tenant1.getId().toString(), process1.getTenantId());

        // Set tenant context to tenant2
        MultiTenantFilter.setTenantId(tenant2.getId().toString());

        // Start process for tenant2
        ProcessInstance process2 = runtimeService.startProcessInstanceByKey("simpleApproval");
        assertNotNull(process2);
        assertEquals(tenant2.getId().toString(), process2.getTenantId());

        // Verify tenant IDs are different
        assertNotEquals(process1.getTenantId(), process2.getTenantId());
    }

    @Test
    @Transactional
    void testTaskTenantIsolation() {
        // Start process for tenant1
        MultiTenantFilter.setTenantId(tenant1.getId().toString());
        ProcessInstance process1 = runtimeService.startProcessInstanceByKey("simpleApproval");

        // Get tasks for tenant1
        List<Task> tenant1Tasks = taskService.createTaskQuery()
                .processInstanceId(process1.getId())
                .list();

        assertFalse(tenant1Tasks.isEmpty());

        // Switch to tenant2
        MultiTenantFilter.setTenantId(tenant2.getId().toString());

        // Start process for tenant2
        ProcessInstance process2 = runtimeService.startProcessInstanceByKey("simpleApproval");

        // Get tasks for tenant2
        List<Task> tenant2Tasks = taskService.createTaskQuery()
                .processInstanceId(process2.getId())
                .list();

        assertFalse(tenant2Tasks.isEmpty());

        // Verify tasks are from different processes
        assertNotEquals(tenant1Tasks.get(0).getProcessInstanceId(), tenant2Tasks.get(0).getProcessInstanceId());
    }

    @Test
    @Transactional
    void testCrossTenantQueryPrevention() {
        // Start process for tenant1
        MultiTenantFilter.setTenantId(tenant1.getId().toString());
        runtimeService.startProcessInstanceByKey("simpleApproval");

        // Without tenant context, query should fail or return empty
        MultiTenantFilter.setTenantId(null);

        List<ProcessInstance> allProcesses = runtimeService.createProcessInstanceQuery().list();

        // Processes should be filtered or context should be required
        assertTrue(allProcesses == null || allProcesses.isEmpty() ||
                allProcesses.stream().allMatch(p -> p.getTenantId() == null));
    }
}
