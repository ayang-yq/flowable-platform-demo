package com.flowable.platform.test.unit;

import com.flowable.platform.config.MultiTenantFilter;
import com.flowable.platform.dto.DecisionExecutionDTO;
import com.flowable.platform.dto.DefinitionDTO;
import com.flowable.platform.dto.DefinitionType;
import com.flowable.platform.service.AuditService;
import com.flowable.platform.service.DecisionService;
import org.flowable.dmn.api.DmnDecision;
import org.flowable.dmn.api.DmnDecisionQuery;
import org.flowable.dmn.api.DmnRepositoryService;
import org.flowable.dmn.api.DmnDecisionService;
import org.flowable.dmn.api.ExecuteDecisionBuilder;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DecisionServiceTest extends AbstractUnitTest {

    @Mock private DmnRepositoryService dmnRepositoryService;
    @Mock private DmnDecisionService dmnRuleService;
    @Mock private AuditService auditService;

    @InjectMocks private DecisionService decisionService;

    @Test
    void listDecisionDefinitions_tenantScoped() {
        try (MockedStatic<MultiTenantFilter> mocked = mockStatic(MultiTenantFilter.class)) {
            mocked.when(MultiTenantFilter::getCurrentTenantId).thenReturn("tenant-1");

            // Tenant-specific query
            DmnDecisionQuery tenantQuery = mock(DmnDecisionQuery.class);
            // Global definitions query
            DmnDecisionQuery globalQuery = mock(DmnDecisionQuery.class);
            when(dmnRepositoryService.createDecisionQuery()).thenReturn(tenantQuery, globalQuery);
            when(tenantQuery.latestVersion()).thenReturn(tenantQuery);
            when(tenantQuery.decisionTenantId("tenant-1")).thenReturn(tenantQuery);
            when(tenantQuery.list()).thenReturn(Collections.emptyList());
            when(globalQuery.latestVersion()).thenReturn(globalQuery);
            when(globalQuery.decisionTenantId("")).thenReturn(globalQuery);
            when(globalQuery.list()).thenReturn(Collections.emptyList());

            List<DefinitionDTO> result = decisionService.listDecisionDefinitions();

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Test
    void executeDecision_happyPath() {
        try (MockedStatic<MultiTenantFilter> mocked = mockStatic(MultiTenantFilter.class)) {
            mocked.when(MultiTenantFilter::getCurrentTenantId).thenReturn("tenant-1");

            // Mock definition lookup
            DmnDecisionQuery defQuery = mock(DmnDecisionQuery.class);
            DmnDecision definition = mock(DmnDecision.class);
            when(dmnRepositoryService.createDecisionQuery()).thenReturn(defQuery);
            when(defQuery.decisionKey("testDecision")).thenReturn(defQuery);
            when(defQuery.latestVersion()).thenReturn(defQuery);
            when(defQuery.decisionTenantId("tenant-1")).thenReturn(defQuery);
            when(defQuery.singleResult()).thenReturn(definition);
            when(definition.getName()).thenReturn("Test Decision");
            when(definition.getId()).thenReturn("dec-1");
            when(definition.getTenantId()).thenReturn("tenant-1");

            // Mock execution
            ExecuteDecisionBuilder execBuilder = mock(ExecuteDecisionBuilder.class);
            when(dmnRuleService.createExecuteDecisionBuilder()).thenReturn(execBuilder);
            when(execBuilder.decisionKey("testDecision")).thenReturn(execBuilder);
            when(execBuilder.variables(anyMap())).thenReturn(execBuilder);
            when(execBuilder.tenantId("tenant-1")).thenReturn(execBuilder);

            List<Map<String, Object>> output = List.of(Map.of("result", true));
            when(execBuilder.execute()).thenReturn(output);

            Map<String, Object> inputs = Map.of("age", 25);
            DecisionExecutionDTO result = decisionService.executeDecision("testDecision", inputs);

            assertNotNull(result);
            assertEquals("testDecision", result.getDecisionKey());
            assertEquals("Test Decision", result.getDecisionName());
            assertEquals(1, result.getOutputVariables().size());
            assertEquals(true, result.getOutputVariables().get(0).get("result"));
            verify(auditService).logAction(eq("DECISION_EXECUTED"), eq("DECISION"), eq("dec-1"), isNull());
        }
    }

    @Test
    void executeDecision_missingDefinition() {
        try (MockedStatic<MultiTenantFilter> mocked = mockStatic(MultiTenantFilter.class)) {
            mocked.when(MultiTenantFilter::getCurrentTenantId).thenReturn("tenant-1");

            // Tenant-specific query returns null
            DmnDecisionQuery tenantDefQuery = mock(DmnDecisionQuery.class);
            // Global fallback query also returns null
            DmnDecisionQuery globalDefQuery = mock(DmnDecisionQuery.class);
            when(dmnRepositoryService.createDecisionQuery()).thenReturn(tenantDefQuery, globalDefQuery);
            when(tenantDefQuery.decisionKey("nonexistent")).thenReturn(tenantDefQuery);
            when(tenantDefQuery.latestVersion()).thenReturn(tenantDefQuery);
            when(tenantDefQuery.decisionTenantId("tenant-1")).thenReturn(tenantDefQuery);
            when(tenantDefQuery.singleResult()).thenReturn(null);
            when(globalDefQuery.decisionKey("nonexistent")).thenReturn(globalDefQuery);
            when(globalDefQuery.latestVersion()).thenReturn(globalDefQuery);
            when(globalDefQuery.decisionTenantId("")).thenReturn(globalDefQuery);
            when(globalDefQuery.singleResult()).thenReturn(null);

            assertThrows(IllegalArgumentException.class, () ->
                    decisionService.executeDecision("nonexistent", Map.of()));
        }
    }
}
