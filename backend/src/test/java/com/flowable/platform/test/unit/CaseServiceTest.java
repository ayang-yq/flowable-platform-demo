package com.flowable.platform.test.unit;

import com.flowable.platform.config.MultiTenantFilter;
import com.flowable.platform.dto.DefinitionDTO;
import com.flowable.platform.dto.DefinitionType;
import com.flowable.platform.dto.InstanceDTO;
import com.flowable.platform.dto.InstanceDetailDTO;
import com.flowable.platform.service.AuditService;
import com.flowable.platform.service.CaseService;
import org.flowable.cmmn.api.CmmnHistoryService;
import org.flowable.cmmn.api.CmmnRepositoryService;
import org.flowable.cmmn.api.CmmnRuntimeService;
import org.flowable.cmmn.api.history.HistoricCaseInstance;
import org.flowable.cmmn.api.history.HistoricCaseInstanceQuery;
import org.flowable.cmmn.api.repository.CaseDefinition;
import org.flowable.cmmn.api.repository.CaseDefinitionQuery;
import org.flowable.cmmn.api.repository.CmmnDeployment;
import org.flowable.cmmn.api.repository.CmmnDeploymentQuery;
import org.flowable.cmmn.api.runtime.CaseInstance;
import org.flowable.cmmn.api.runtime.CaseInstanceBuilder;
import org.flowable.cmmn.api.runtime.CaseInstanceQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CaseServiceTest extends AbstractUnitTest {

    @Mock private CmmnRuntimeService cmmnRuntimeService;
    @Mock private CmmnHistoryService cmmnHistoryService;
    @Mock private CmmnRepositoryService cmmnRepositoryService;
    @Mock private AuditService auditService;

    @InjectMocks private CaseService caseService;

    @Test
    void listCaseDefinitions_tenantScoped() {
        try (MockedStatic<MultiTenantFilter> mocked = mockStatic(MultiTenantFilter.class)) {
            mocked.when(MultiTenantFilter::getCurrentTenantId).thenReturn("tenant-1");

            CaseDefinitionQuery query = mock(CaseDefinitionQuery.class);
            when(cmmnRepositoryService.createCaseDefinitionQuery()).thenReturn(query);
            when(query.latestVersion()).thenReturn(query);
            when(query.caseDefinitionTenantId("tenant-1")).thenReturn(query);
            when(query.list()).thenReturn(Collections.emptyList());

            List<DefinitionDTO> result = caseService.listCaseDefinitions();

            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(query).caseDefinitionTenantId("tenant-1");
        }
    }

    @Test
    void startCaseInstance_happyPath() {
        try (MockedStatic<MultiTenantFilter> mocked = mockStatic(MultiTenantFilter.class)) {
            mocked.when(MultiTenantFilter::getCurrentTenantId).thenReturn("tenant-1");

            CaseDefinitionQuery defQuery = mock(CaseDefinitionQuery.class);
            CaseDefinition definition = mock(CaseDefinition.class);
            when(cmmnRepositoryService.createCaseDefinitionQuery()).thenReturn(defQuery);
            when(defQuery.caseDefinitionKey("testCase")).thenReturn(defQuery);
            when(defQuery.latestVersion()).thenReturn(defQuery);
            when(defQuery.caseDefinitionTenantId("tenant-1")).thenReturn(defQuery);
            when(defQuery.singleResult()).thenReturn(definition);
            when(definition.getName()).thenReturn("Test Case");

            CaseInstanceBuilder builder = mock(CaseInstanceBuilder.class);
            CaseInstance caseInstance = mock(CaseInstance.class);
            when(cmmnRuntimeService.createCaseInstanceBuilder()).thenReturn(builder);
            when(builder.caseDefinitionKey("testCase")).thenReturn(builder);
            when(builder.variables(anyMap())).thenReturn(builder);
            when(builder.businessKey(anyString())).thenReturn(builder);
            when(builder.tenantId("tenant-1")).thenReturn(builder);
            when(builder.start()).thenReturn(caseInstance);
            when(caseInstance.getId()).thenReturn("case-1");
            when(caseInstance.getCaseDefinitionId()).thenReturn("testCase:1:1");
            when(caseInstance.getCaseDefinitionKey()).thenReturn("testCase");

            InstanceDTO result = caseService.startCaseInstance("testCase", Map.of("key", "value"), "BK-001");

            assertNotNull(result);
            assertEquals("case-1", result.getId());
            assertEquals(DefinitionType.CMMN, result.getType());
            verify(auditService).logAction(eq("CASE_STARTED"), eq("CASE_INSTANCE"), eq("case-1"), isNull());
        }
    }

    @Test
    void startCaseInstance_missingDefinition() {
        try (MockedStatic<MultiTenantFilter> mocked = mockStatic(MultiTenantFilter.class)) {
            mocked.when(MultiTenantFilter::getCurrentTenantId).thenReturn("tenant-1");

            CaseDefinitionQuery defQuery = mock(CaseDefinitionQuery.class);
            when(cmmnRepositoryService.createCaseDefinitionQuery()).thenReturn(defQuery);
            when(defQuery.caseDefinitionKey("nonexistent")).thenReturn(defQuery);
            when(defQuery.latestVersion()).thenReturn(defQuery);
            when(defQuery.caseDefinitionTenantId("tenant-1")).thenReturn(defQuery);
            when(defQuery.singleResult()).thenReturn(null);

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
            when(query.caseInstanceTenantId("tenant-1")).thenReturn(query);
            when(query.count()).thenReturn(5L);

            long count = caseService.countActiveCaseInstances(null);

            assertEquals(5L, count);
            verify(query).caseInstanceTenantId("tenant-1");
        }
    }

    @Test
    void countCompletedCaseInstances_tenantScoped() {
        try (MockedStatic<MultiTenantFilter> mocked = mockStatic(MultiTenantFilter.class)) {
            mocked.when(MultiTenantFilter::getCurrentTenantId).thenReturn("tenant-1");

            HistoricCaseInstanceQuery query = mock(HistoricCaseInstanceQuery.class);
            when(cmmnHistoryService.createHistoricCaseInstanceQuery()).thenReturn(query);
            when(query.finished()).thenReturn(query);
            when(query.caseInstanceTenantId("tenant-1")).thenReturn(query);
            when(query.count()).thenReturn(10L);

            long count = caseService.countCompletedCaseInstances();

            assertEquals(10L, count);
        }
    }
}
