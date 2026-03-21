package com.flowable.platform.integration;

import org.flowable.cmmn.api.CmmnRepositoryService;
import org.flowable.cmmn.api.CmmnRuntimeService;
import org.flowable.cmmn.api.CmmnTaskService;
import org.flowable.cmmn.api.runtime.CaseInstance;
import org.flowable.cmmn.engine.test.CmmnDeployment;
import org.flowable.cmmn.spring.impl.test.FlowableCmmnSpringExtension;
import org.flowable.task.api.Task;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@ExtendWith(FlowableCmmnSpringExtension.class)
class CmmnAdHocTaskTest {

    @Autowired
    private CmmnRepositoryService cmmnRepositoryService;

    @Autowired
    private CmmnRuntimeService cmmnRuntimeService;

    @Autowired
    private CmmnTaskService cmmnTaskService;

    @Test
    @CmmnDeployment(resources = {"processes/ad-hoc-case.cmmn"})
    void testCreateAdHocTaskInCaseInstance() {
        // Start case instance
        CaseInstance caseInstance = cmmnRuntimeService.createCaseInstanceBuilder()
                .caseDefinitionKey("adHocCase")
                .start();

        assertNotNull(caseInstance);
        assertNotNull(caseInstance.getId());

        // Get existing tasks
        List<Task> initialTasks = cmmnTaskService.createTaskQuery()
                .caseInstanceId(caseInstance.getId())
                .list();

        int initialTaskCount = initialTasks.size();

        // Create ad-hoc task dynamically
        String adHocTaskName = "Additional Review";
        cmmnRuntimeService.createPlanItemInstanceBuilder()
                .caseInstanceId(caseInstance.getId())
                .elementId("adHocTask")
                .name(adHocTaskName)
                .create();

        // Verify ad-hoc task was created
        List<Task> tasksAfterAdHoc = cmmnTaskService.createTaskQuery()
                .caseInstanceId(caseInstance.getId())
                .list();

        assertTrue(tasksAfterAdHoc.size() > initialTaskCount);

        // Verify the ad-hoc task exists
        boolean adHocTaskExists = tasksAfterAdHoc.stream()
                .anyMatch(task -> task.getName().equals(adHocTaskName));
        assertTrue(adHocTaskExists);
    }

    @Test
    @CmmnDeployment(resources = {"processes/ad-hoc-case.cmmn"})
    void testCompleteAdHocTask() {
        // Start case instance
        CaseInstance caseInstance = cmmnRuntimeService.createCaseInstanceBuilder()
                .caseDefinitionKey("adHocCase")
                .start();

        // Create ad-hoc task
        cmmnRuntimeService.createPlanItemInstanceBuilder()
                .caseInstanceId(caseInstance.getId())
                .elementId("adHocTask")
                .name("Ad-hoc Review")
                .create();

        // Get the ad-hoc task
        List<Task> tasks = cmmnTaskService.createTaskQuery()
                .caseInstanceId(caseInstance.getId())
                .taskName("Ad-hoc Review")
                .list();

        assertFalse(tasks.isEmpty());
        Task adHocTask = tasks.get(0);

        // Complete the ad-hoc task
        cmmnTaskService.complete(adHocTask.getId());

        // Verify task completion
        List<Task> remainingTasks = cmmnTaskService.createTaskQuery()
                .caseInstanceId(caseInstance.getId())
                .taskName("Ad-hoc Review")
                .list();

        assertTrue(remainingTasks.isEmpty());
    }

    @Test
    @CmmnDeployment(resources = {"processes/ad-hoc-case.cmmn"})
    void testMultipleAdHocTasksInSameCase() {
        // Start case instance
        CaseInstance caseInstance = cmmnRuntimeService.createCaseInstanceBuilder()
                .caseDefinitionKey("adHocCase")
                .start();

        // Create multiple ad-hoc tasks
        cmmnRuntimeService.createPlanItemInstanceBuilder()
                .caseInstanceId(caseInstance.getId())
                .elementId("adHocTask1")
                .name("First Review")
                .create();

        cmmnRuntimeService.createPlanItemInstanceBuilder()
                .caseInstanceId(caseInstance.getId())
                .elementId("adHocTask2")
                .name("Second Review")
                .create();

        cmmnRuntimeService.createPlanItemInstanceBuilder()
                .caseInstanceId(caseInstance.getId())
                .elementId("adHocTask3")
                .name("Third Review")
                .create();

        // Verify all ad-hoc tasks were created
        List<Task> tasks = cmmnTaskService.createTaskQuery()
                .caseInstanceId(caseInstance.getId())
                .list();

        assertTrue(tasks.size() >= 3);

        boolean hasFirst = tasks.stream().anyMatch(t -> "First Review".equals(t.getName()));
        boolean hasSecond = tasks.stream().anyMatch(t -> "Second Review".equals(t.getName()));
        boolean hasThird = tasks.stream().anyMatch(t -> "Third Review".equals(t.getName()));

        assertTrue(hasFirst && hasSecond && hasThird);
    }
}
