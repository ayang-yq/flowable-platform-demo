package com.flowable.platform.integration;

import org.flowable.cmmn.api.CmmnRepositoryService;
import org.flowable.cmmn.api.CmmnRuntimeService;
import org.flowable.cmmn.api.CmmnTaskService;
import org.flowable.cmmn.api.repository.CaseDefinition;
import org.flowable.cmmn.api.runtime.CaseInstance;
import org.flowable.cmmn.engine.test.CmmnDeployment;
import org.flowable.cmmn.spring.impl.test.FlowableCmmnSpringExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@ExtendWith(FlowableCmmnSpringExtension.class)
class CmmnIntegrationTest {

    @Autowired
    private CmmnRepositoryService cmmnRepositoryService;

    @Autowired
    private CmmnRuntimeService cmmnRuntimeService;

    @Autowired
    private CmmnTaskService cmmnTaskService;

    @Test
    @CmmnDeployment(resources = {"processes/simple-case.cmmn"})
    void testDeployCaseDefinition() {
        // Verify case definition deployment
        CaseDefinition caseDefinition = cmmnRepositoryService.createCaseDefinitionQuery()
                .caseDefinitionKey("simpleCase")
                .singleResult();

        assertNotNull(caseDefinition);
        assertEquals("Simple Case", caseDefinition.getName());
    }

    @Test
    @CmmnDeployment(resources = {"processes/simple-case.cmmn"})
    void testStartCaseInstance() {
        // Start case instance with variables
        Map<String, Object> variables = new HashMap<>();
        variables.put("customerName", "John Doe");
        variables.put("caseType", "Complaint");

        CaseInstance caseInstance = cmmnRuntimeService.createCaseInstanceBuilder()
                .caseDefinitionKey("simpleCase")
                .variables(variables)
                .start();

        // Verify case instance
        assertNotNull(caseInstance);
        assertNotNull(caseInstance.getId());
        assertEquals("simpleCase", caseInstance.getCaseDefinitionKey());
    }

    @Test
    @CmmnDeployment(resources = {"processes/simple-case.cmmn"})
    void testCompleteCaseTask() {
        // Start case instance
        CaseInstance caseInstance = cmmnRuntimeService.createCaseInstanceBuilder()
                .caseDefinitionKey("simpleCase")
                .start();

        // Get human task
        var tasks = cmmnTaskService.createTaskQuery()
                .caseInstanceId(caseInstance.getId())
                .list();

        assertFalse(tasks.isEmpty());

        // Complete task
        cmmnTaskService.complete(tasks.get(0).getId());

        // Verify task completion
        var remainingTasks = cmmnTaskService.createTaskQuery()
                .caseInstanceId(caseInstance.getId())
                .list();

        assertTrue(remainingTasks.isEmpty() || remainingTasks.size() < tasks.size());
    }
}
