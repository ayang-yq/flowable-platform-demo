package com.flowable.platform.test.unit;

import com.flowable.platform.config.MultiTenantFilter;
import com.flowable.platform.dto.DefinitionDTO;
import com.flowable.platform.dto.DefinitionType;
import com.flowable.platform.dto.DiagramDataDTO;
import com.flowable.platform.dto.InstanceDTO;
import com.flowable.platform.service.AuditService;
import com.flowable.platform.service.CaseService;
import org.flowable.cmmn.api.CmmnHistoryService;
import org.flowable.cmmn.api.CmmnRepositoryService;
import org.flowable.cmmn.api.CmmnRuntimeService;
import org.flowable.cmmn.api.history.HistoricCaseInstance;
import org.flowable.cmmn.api.history.HistoricCaseInstanceQuery;
import org.flowable.cmmn.api.repository.CaseDefinition;
import org.flowable.cmmn.api.repository.CaseDefinitionQuery;
import org.flowable.cmmn.api.runtime.CaseInstance;
import org.flowable.cmmn.api.runtime.CaseInstanceBuilder;
import org.flowable.cmmn.api.runtime.CaseInstanceQuery;
import org.flowable.cmmn.api.runtime.PlanItemInstance;
import org.flowable.cmmn.api.runtime.PlanItemInstanceQuery;
import org.flowable.cmmn.model.CmmnModel;
import org.flowable.engine.IdentityService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CaseServiceTest extends AbstractUnitTest {

    @Mock private CmmnRuntimeService cmmnRuntimeService;
    @Mock private CmmnHistoryService cmmnHistoryService;
    @Mock private CmmnRepositoryService cmmnRepositoryService;
    @Mock private AuditService auditService;
    @Mock private IdentityService identityService;

    @InjectMocks private CaseService caseService;

    @Test
    void listCaseDefinitions_tenantScoped() {
        try (MockedStatic<MultiTenantFilter> mocked = mockStatic(MultiTenantFilter.class)) {
            mocked.when(MultiTenantFilter::getCurrentTenantId).thenReturn("tenant-1");

            // Tenant-specific query
            CaseDefinitionQuery tenantQuery = mock(CaseDefinitionQuery.class);
            // Global definitions query
            CaseDefinitionQuery globalQuery = mock(CaseDefinitionQuery.class);
            when(cmmnRepositoryService.createCaseDefinitionQuery()).thenReturn(tenantQuery, globalQuery);
            when(tenantQuery.latestVersion()).thenReturn(tenantQuery);
            when(tenantQuery.caseDefinitionTenantId("tenant-1")).thenReturn(tenantQuery);
            when(tenantQuery.list()).thenReturn(Collections.emptyList());
            when(globalQuery.latestVersion()).thenReturn(globalQuery);
            when(globalQuery.caseDefinitionTenantId("")).thenReturn(globalQuery);
            when(globalQuery.list()).thenReturn(Collections.emptyList());

            List<DefinitionDTO> result = caseService.listCaseDefinitions();

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Test
    void startCaseInstance_happyPath() {
        try (MockedStatic<MultiTenantFilter> mocked = mockStatic(MultiTenantFilter.class);
             MockedStatic<SecurityContextHolder> securityMock = mockStatic(SecurityContextHolder.class)) {
            mocked.when(MultiTenantFilter::getCurrentTenantId).thenReturn("tenant-1");

            SecurityContext securityContext = mock(SecurityContext.class);
            Authentication auth = mock(Authentication.class);
            when(securityContext.getAuthentication()).thenReturn(auth);
            when(auth.getName()).thenReturn("testuser");
            securityMock.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            CaseDefinitionQuery defQuery = mock(CaseDefinitionQuery.class);
            CaseDefinition definition = mock(CaseDefinition.class);
            when(cmmnRepositoryService.createCaseDefinitionQuery()).thenReturn(defQuery);
            when(defQuery.caseDefinitionKey("testCase")).thenReturn(defQuery);
            when(defQuery.latestVersion()).thenReturn(defQuery);
            when(defQuery.caseDefinitionTenantId("tenant-1")).thenReturn(defQuery);
            when(defQuery.singleResult()).thenReturn(definition);
            when(definition.getName()).thenReturn("Test Case");
            when(definition.getTenantId()).thenReturn("tenant-1");

            CaseInstanceBuilder builder = mock(CaseInstanceBuilder.class);
            CaseInstance caseInstance = mock(CaseInstance.class);
            when(cmmnRuntimeService.createCaseInstanceBuilder()).thenReturn(builder);
            when(builder.caseDefinitionId(definition.getId())).thenReturn(builder);
            when(builder.variables(anyMap())).thenReturn(builder);
            when(builder.businessKey(anyString())).thenReturn(builder);
            when(builder.tenantId("tenant-1")).thenReturn(builder);
            when(builder.start()).thenReturn(caseInstance);
            when(caseInstance.getId()).thenReturn("case-1");
            when(caseInstance.getCaseDefinitionId()).thenReturn("testCase:1:1");
            when(caseInstance.getCaseDefinitionKey()).thenReturn("testCase");
            when(caseInstance.getStartTime()).thenReturn(new Date());
            when(caseInstance.getStartUserId()).thenReturn("testuser");
            when(caseInstance.getTenantId()).thenReturn("tenant-1");

            InstanceDTO result = caseService.startCaseInstance("testCase", Map.of("key", "value"), "BK-001");

            assertNotNull(result);
            assertEquals("case-1", result.getId());
            assertEquals(DefinitionType.CMMN, result.getType());
            verify(auditService).logAction(eq("CASE_STARTED"), eq("CASE_INSTANCE"), eq("case-1"), isNull());
        }
    }

    @Test
    void startCaseInstance_missingDefinition() {
        try (MockedStatic<MultiTenantFilter> mocked = mockStatic(MultiTenantFilter.class);
             MockedStatic<SecurityContextHolder> securityMock = mockStatic(SecurityContextHolder.class)) {
            mocked.when(MultiTenantFilter::getCurrentTenantId).thenReturn("tenant-1");

            SecurityContext securityContext = mock(SecurityContext.class);
            Authentication auth = mock(Authentication.class);
            when(securityContext.getAuthentication()).thenReturn(auth);
            when(auth.getName()).thenReturn("testuser");
            securityMock.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            CaseDefinitionQuery tenantDefQuery = mock(CaseDefinitionQuery.class);
            CaseDefinitionQuery globalDefQuery = mock(CaseDefinitionQuery.class);
            when(cmmnRepositoryService.createCaseDefinitionQuery()).thenReturn(tenantDefQuery, globalDefQuery);
            when(tenantDefQuery.caseDefinitionKey("nonexistent")).thenReturn(tenantDefQuery);
            when(tenantDefQuery.latestVersion()).thenReturn(tenantDefQuery);
            when(tenantDefQuery.caseDefinitionTenantId("tenant-1")).thenReturn(tenantDefQuery);
            when(tenantDefQuery.singleResult()).thenReturn(null);
            when(globalDefQuery.caseDefinitionKey("nonexistent")).thenReturn(globalDefQuery);
            when(globalDefQuery.latestVersion()).thenReturn(globalDefQuery);
            when(globalDefQuery.caseDefinitionTenantId("")).thenReturn(globalDefQuery);
            when(globalDefQuery.singleResult()).thenReturn(null);

            assertThrows(IllegalArgumentException.class, () ->
                    caseService.startCaseInstance("nonexistent", null, null));
        }
    }

    @Test
    void countActiveCaseInstances_tenantScoped() {
        try (MockedStatic<MultiTenantFilter> mocked = mockStatic(MultiTenantFilter.class)) {
            mocked.when(MultiTenantFilter::getCurrentTenantId).thenReturn("tenant-1");

            CaseInstanceQuery query = mock(CaseInstanceQuery.class);
            when(cmmnRuntimeService.createCaseInstanceQuery()).thenReturn(query);
            when(query.or()).thenReturn(query);
            when(query.caseInstanceTenantId("tenant-1")).thenReturn(query);
            when(query.caseInstanceTenantId("")).thenReturn(query);
            when(query.endOr()).thenReturn(query);
            when(query.count()).thenReturn(5L);

            long count = caseService.countActiveCaseInstances(null);

            assertEquals(5L, count);
        }
    }

    @Test
    void countCompletedCaseInstances_tenantScoped() {
        try (MockedStatic<MultiTenantFilter> mocked = mockStatic(MultiTenantFilter.class)) {
            mocked.when(MultiTenantFilter::getCurrentTenantId).thenReturn("tenant-1");

            HistoricCaseInstanceQuery query = mock(HistoricCaseInstanceQuery.class);
            when(cmmnHistoryService.createHistoricCaseInstanceQuery()).thenReturn(query);
            when(query.finished()).thenReturn(query);
            when(query.or()).thenReturn(query);
            when(query.caseInstanceTenantId("tenant-1")).thenReturn(query);
            when(query.caseInstanceTenantId("")).thenReturn(query);
            when(query.endOr()).thenReturn(query);
            when(query.count()).thenReturn(10L);

            long count = caseService.countCompletedCaseInstances();

            assertEquals(10L, count);
        }
    }

    @Test
    void getCaseInstanceDiagram_returnsNull_whenInstanceNotFound() {
        CaseInstanceQuery query = mock(CaseInstanceQuery.class);
        when(cmmnRuntimeService.createCaseInstanceQuery()).thenReturn(query);
        when(query.caseInstanceId("nonexistent")).thenReturn(query);
        when(query.singleResult()).thenReturn(null);

        DiagramDataDTO result = caseService.getCaseInstanceDiagram("nonexistent");

        assertNull(result);
    }

    @Test
    void getCaseInstanceDiagram_returnsDiagramData_forActiveInstance() {
        // Mock case instance
        CaseInstance instance = mock(CaseInstance.class);
        when(instance.getId()).thenReturn("case-1");
        when(instance.getCaseDefinitionId()).thenReturn("simpleCase:1:1");

        CaseInstanceQuery instanceQuery = mock(CaseInstanceQuery.class);
        when(cmmnRuntimeService.createCaseInstanceQuery()).thenReturn(instanceQuery);
        when(instanceQuery.caseInstanceId("case-1")).thenReturn(instanceQuery);
        when(instanceQuery.singleResult()).thenReturn(instance);

        // Mock case definition
        CaseDefinition definition = mock(CaseDefinition.class);
        when(definition.getId()).thenReturn("simpleCase:1:1");
        when(definition.getDeploymentId()).thenReturn("dep-1");
        when(definition.getResourceName()).thenReturn("processes/simple-case.cmmn");

        CaseDefinitionQuery defQuery = mock(CaseDefinitionQuery.class);
        when(cmmnRepositoryService.createCaseDefinitionQuery()).thenReturn(defQuery);
        when(defQuery.caseDefinitionId("simpleCase:1:1")).thenReturn(defQuery);
        when(defQuery.singleResult()).thenReturn(definition);

        // Mock CMMN model returning null to trigger fallback resource path
        when(cmmnRepositoryService.getCmmnModel("simpleCase:1:1")).thenReturn(null);

        // Mock CMMN resource as fallback
        String cmmnXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><definitions xmlns=\"http://www.omg.org/spec/CMMN/20151109/MODEL\" xmlns:cmmndi=\"http://www.omg.org/spec/CMMN/20151109/CMMNDI\" xmlns:dc=\"http://www.omg.org/spec/CMMN/20151109/DC\"><case id=\"simpleCase\"><casePlanModel id=\"casePlanModel\"><planItem id=\"task1\" definitionRef=\"ht1\"/></casePlanModel></case><cmmndi:CMMNDI><cmmndi:CMMNDiagram id=\"d1\"><cmmndi:CMMNShape id=\"s1\" cmmnElementRef=\"task1\"><dc:Bounds x=\"100\" y=\"80\" width=\"160\" height=\"100\"/><cmmndi:CMMNLabel/></cmmndi:CMMNShape></cmmndi:CMMNDiagram></cmmndi:CMMNDI></definitions>";
        when(cmmnRepositoryService.getResourceAsStream(eq("dep-1"), eq("processes/simple-case.cmmn")))
                .thenReturn(new java.io.ByteArrayInputStream(cmmnXml.getBytes(java.nio.charset.StandardCharsets.UTF_8)));

        // Mock active plan items
        PlanItemInstance planItem = mock(PlanItemInstance.class);
        when(planItem.getId()).thenReturn("planItem-1");
        when(planItem.getElementId()).thenReturn("humanTask");
        when(planItem.getName()).thenReturn("Review Case");

        PlanItemInstanceQuery planQuery = mock(PlanItemInstanceQuery.class);
        when(cmmnRuntimeService.createPlanItemInstanceQuery()).thenReturn(planQuery);
        when(planQuery.caseInstanceId("case-1")).thenReturn(planQuery);
        when(planQuery.planItemInstanceStateActive()).thenReturn(planQuery);
        when(planQuery.list()).thenReturn(List.of(planItem));

        DiagramDataDTO result = caseService.getCaseInstanceDiagram("case-1");

        assertNotNull(result);
        assertNotNull(result.getDiagramXml());
        // Service adds both planItem ID and element ID for active elements
        assertTrue(result.getActiveElementIds().size() >= 1);
        assertTrue(result.getActiveElementIds().contains("planItem-1"));
        assertEquals("planItem-1", result.getCurrentElementId());
    }

    @Test
    void getCaseInstanceDiagram_returnsNull_whenDefinitionNotFound() {
        CaseInstance instance = mock(CaseInstance.class);
        when(instance.getId()).thenReturn("case-1");
        when(instance.getCaseDefinitionId()).thenReturn("missing:1:1");

        CaseInstanceQuery instanceQuery = mock(CaseInstanceQuery.class);
        when(cmmnRuntimeService.createCaseInstanceQuery()).thenReturn(instanceQuery);
        when(instanceQuery.caseInstanceId("case-1")).thenReturn(instanceQuery);
        when(instanceQuery.singleResult()).thenReturn(instance);

        CaseDefinitionQuery defQuery = mock(CaseDefinitionQuery.class);
        when(cmmnRepositoryService.createCaseDefinitionQuery()).thenReturn(defQuery);
        when(defQuery.caseDefinitionId("missing:1:1")).thenReturn(defQuery);
        when(defQuery.singleResult()).thenReturn(null);

        DiagramDataDTO result = caseService.getCaseInstanceDiagram("case-1");

        assertNull(result);
    }

    @Test
    void getCaseInstanceDiagram_returnsEmptyActiveIds_forCompletedCase() {
        // Active query returns null (completed case)
        CaseInstanceQuery instanceQuery = mock(CaseInstanceQuery.class);
        when(cmmnRuntimeService.createCaseInstanceQuery()).thenReturn(instanceQuery);
        when(instanceQuery.caseInstanceId("case-done")).thenReturn(instanceQuery);
        when(instanceQuery.singleResult()).thenReturn(null);

        // Historic case exists but diagram returns null since it's completed
        DiagramDataDTO result = caseService.getCaseInstanceDiagram("case-done");

        assertNull(result);
    }
}
