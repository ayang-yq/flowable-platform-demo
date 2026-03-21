package com.flowable.platform.integration;

import org.flowable.dmn.api.DmnRepositoryService;
import org.flowable.dmn.api.DmnRuntimeService;
import org.flowable.dmn.api.DmnDeployment;
import org.flowable.dmn.engine.test.FlowableDmnExtension;
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
@ExtendWith(FlowableDmnExtension.class)
class DmnIntegrationTest {

    @Autowired
    private DmnRepositoryService dmnRepositoryService;

    @Autowired
    private DmnRuntimeService dmnRuntimeService;

    @Test
    @DmnDeployment(resources = {"processes/simple-decision.dmn"})
    void testDeployDecisionTable() {
        // Verify decision table deployment
        var decision = dmnRepositoryService.createDecisionQuery()
                .decisionKey("simpleDecision")
                .singleResult();

        assertNotNull(decision);
        assertEquals("Simple Decision", decision.getName());
    }

    @Test
    @DmnDeployment(resources = {"processes/simple-decision.dmn"})
    void testEvaluateDecisionTable() {
        // Create input variables
        Map<String, Object> variables = new HashMap<>();
        variables.put("age", 25);
        variables.put("income", 50000);

        // Evaluate decision
        var result = dmnRuntimeService.createDecisionTableExecutionBuilder()
                .decisionKey("simpleDecision")
                .variables(variables)
                .executeWithSingleResult();

        // Verify result
        assertNotNull(result);
        assertTrue(result.containsEntry("approval", true));
    }

    @Test
    @DmnDeployment(resources = {"processes/simple-decision.dmn"})
    void testEvaluateDecisionWithMultipleRules() {
        // Test rule 1: High income, approved
        Map<String, Object> vars1 = new HashMap<>();
        vars1.put("age", 30);
        vars1.put("income", 75000);
        var result1 = dmnRuntimeService.createDecisionTableExecutionBuilder()
                .decisionKey("simpleDecision")
                .variables(vars1)
                .executeWithSingleResult();
        assertEquals(true, result1.get("approval"));

        // Test rule 2: Low income, rejected
        Map<String, Object> vars2 = new HashMap<>();
        vars2.put("age", 25);
        vars2.put("income", 25000);
        var result2 = dmnRuntimeService.createDecisionTableExecutionBuilder()
                .decisionKey("simpleDecision")
                .variables(vars2)
                .executeWithSingleResult();
        assertEquals(false, result2.get("approval"));
    }
}
