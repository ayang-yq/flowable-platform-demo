package com.flowable.platform.test.unit;

import com.flowable.platform.config.MultiTenantFilter;
import com.flowable.platform.dto.*;
import com.flowable.platform.service.CaseService;
import com.flowable.platform.service.DecisionService;
import com.flowable.platform.service.WorkspaceService;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricProcessInstanceQuery;
import org.flowable.engine.repository.ProcessDefinitionQuery;
import org.flowable.engine.runtime.ProcessInstanceQuery;
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

class WorkspaceServiceTest extends AbstractUnitTest {

    @Mock private RuntimeService runtimeService;
    @Mock private RepositoryService repositoryService;
    @Mock private HistoryService historyService;
    @Mock private TaskService taskService;
    @Mock private CaseService caseService;
    @Mock private DecisionService decisionService;

    @InjectMocks private WorkspaceService workspaceService;

    @Test
    void getAllDefinitions_mergesBpmnCmmnDmn() {
        try (MockedStatic<MultiTenantFilter> mocked = mockStatic(MultiTenantFilter.class)) {
            mocked.when(MultiTenantFilter::getCurrentTenantId).thenReturn("tenant-1");

            // Mock BPMN definitions - two queries: tenant-specific and global
            ProcessDefinitionQuery bpmnTenantQuery = mock(ProcessDefinitionQuery.class);
            ProcessDefinitionQuery bpmnGlobalQuery = mock(ProcessDefinitionQuery.class);
            when(repositoryService.createProcessDefinitionQuery()).thenReturn(bpmnTenantQuery, bpmnGlobalQuery);
            when(bpmnTenantQuery.processDefinitionTenantId("tenant-1")).thenReturn(bpmnTenantQuery);
            when(bpmnTenantQuery.latestVersion()).thenReturn(bpmnTenantQuery);
            when(bpmnTenantQuery.list()).thenReturn(Collections.emptyList());
            when(bpmnGlobalQuery.processDefinitionTenantId("")).thenReturn(bpmnGlobalQuery);
            when(bpmnGlobalQuery.latestVersion()).thenReturn(bpmnGlobalQuery);
            when(bpmnGlobalQuery.list()).thenReturn(Collections.emptyList());

            // Mock CMMN definitions
            List<DefinitionDTO> cmmnDefs = List.of(
                    new DefinitionDTO("c1", "case1", "Case 1", 1, null, DefinitionType.CMMN, false, null));
            when(caseService.listCaseDefinitions()).thenReturn(cmmnDefs);

            // Mock DMN definitions
            List<DefinitionDTO> dmnDefs = List.of(
                    new DefinitionDTO("d1", "dec1", "Decision 1", 1, null, DefinitionType.DMN, false, null));
            when(decisionService.listDecisionDefinitions()).thenReturn(dmnDefs);

            List<DefinitionDTO> result = workspaceService.getAllDefinitions("all");

            assertEquals(2, result.size());
            verify(caseService).listCaseDefinitions();
            verify(decisionService).listDecisionDefinitions();
        }
    }

    @Test
    void getAllDefinitions_filterByType() {
        try (MockedStatic<MultiTenantFilter> mocked = mockStatic(MultiTenantFilter.class)) {
            mocked.when(MultiTenantFilter::getCurrentTenantId).thenReturn("tenant-1");

            List<DefinitionDTO> cmmnDefs = List.of(
                    new DefinitionDTO("c1", "case1", "Case 1", 1, null, DefinitionType.CMMN, false, null));
            when(caseService.listCaseDefinitions()).thenReturn(cmmnDefs);

            List<DefinitionDTO> result = workspaceService.getAllDefinitions("CMMN");

            assertEquals(1, result.size());
            assertEquals(DefinitionType.CMMN, result.get(0).getType());
            verify(decisionService, never()).listDecisionDefinitions();
        }
    }

    @Test
    void getActiveInstances_mergedPagination() {
        try (MockedStatic<MultiTenantFilter> mocked = mockStatic(MultiTenantFilter.class)) {
            mocked.when(MultiTenantFilter::getCurrentTenantId).thenReturn("tenant-1");

            // Mock BPMN active instances with or()/endOr() chain
            ProcessInstanceQuery bpmnQuery = mock(ProcessInstanceQuery.class);
            when(runtimeService.createProcessInstanceQuery()).thenReturn(bpmnQuery);
            when(bpmnQuery.or()).thenReturn(bpmnQuery);
            when(bpmnQuery.processInstanceTenantId("tenant-1")).thenReturn(bpmnQuery);
            when(bpmnQuery.processInstanceTenantId("")).thenReturn(bpmnQuery);
            when(bpmnQuery.endOr()).thenReturn(bpmnQuery);
            when(bpmnQuery.orderByProcessInstanceId()).thenReturn(bpmnQuery);
            when(bpmnQuery.desc()).thenReturn(bpmnQuery);
            when(bpmnQuery.listPage(0, 20)).thenReturn(Collections.emptyList());
            when(bpmnQuery.count()).thenReturn(0L);

            // Mock CMMN active instances
            when(caseService.listActiveCaseInstances(eq(0), eq(20), isNull(), isNull(), isNull(), isNull()))
                    .thenReturn(Collections.emptyList());
            when(caseService.countActiveCaseInstances(isNull())).thenReturn(0L);

            InstancePageDTO result = workspaceService.getActiveInstances(0, 20, "all", null, null, null, null);

            assertNotNull(result);
            assertEquals(0, result.getTotalElements());
            assertTrue(result.getContent().isEmpty());
        }
    }

    @Test
    void getDashboardSummary_correctCounts() {
        try (MockedStatic<MultiTenantFilter> mocked = mockStatic(MultiTenantFilter.class);
             MockedStatic<SecurityContextHolder> securityMock = mockStatic(SecurityContextHolder.class)) {
            mocked.when(MultiTenantFilter::getCurrentTenantId).thenReturn("tenant-1");

            SecurityContext securityContext = mock(SecurityContext.class);
            Authentication auth = mock(Authentication.class);
            when(securityContext.getAuthentication()).thenReturn(auth);
            when(auth.getName()).thenReturn("testuser");
            securityMock.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // Active BPMN - countActiveBpmnInstances(null)
            ProcessInstanceQuery activeQueryNull = mock(ProcessInstanceQuery.class);
            when(activeQueryNull.or()).thenReturn(activeQueryNull);
            when(activeQueryNull.processInstanceTenantId("tenant-1")).thenReturn(activeQueryNull);
            when(activeQueryNull.processInstanceTenantId("")).thenReturn(activeQueryNull);
            when(activeQueryNull.endOr()).thenReturn(activeQueryNull);
            when(activeQueryNull.count()).thenReturn(3L);

            // Active CMMN
            when(caseService.countActiveCaseInstances(null)).thenReturn(2L);

            // Completed BPMN
            HistoricProcessInstanceQuery histQuery = mock(HistoricProcessInstanceQuery.class);
            when(histQuery.finished()).thenReturn(histQuery);
            when(histQuery.or()).thenReturn(histQuery);
            when(histQuery.processInstanceTenantId("tenant-1")).thenReturn(histQuery);
            when(histQuery.processInstanceTenantId("")).thenReturn(histQuery);
            when(histQuery.endOr()).thenReturn(histQuery);
            when(histQuery.count()).thenReturn(10L);

            // Completed CMMN
            when(caseService.countCompletedCaseInstances()).thenReturn(5L);

            // My active BPMN - countActiveBpmnInstances("testuser")
            ProcessInstanceQuery myActiveQuery = mock(ProcessInstanceQuery.class);
            when(myActiveQuery.or()).thenReturn(myActiveQuery);
            when(myActiveQuery.processInstanceTenantId("tenant-1")).thenReturn(myActiveQuery);
            when(myActiveQuery.processInstanceTenantId("")).thenReturn(myActiveQuery);
            when(myActiveQuery.endOr()).thenReturn(myActiveQuery);
            when(myActiveQuery.startedBy("testuser")).thenReturn(myActiveQuery);
            when(myActiveQuery.count()).thenReturn(1L);

            // Sequential returns: first call -> activeQueryNull, second call -> myActiveQuery
            when(runtimeService.createProcessInstanceQuery()).thenReturn(activeQueryNull, myActiveQuery);

            // Sequential returns for historyService: completed + started today
            HistoricProcessInstanceQuery todayQuery = mock(HistoricProcessInstanceQuery.class);
            when(todayQuery.startedAfter(any())).thenReturn(todayQuery);
            when(todayQuery.count()).thenReturn(2L);
            when(historyService.createHistoricProcessInstanceQuery()).thenReturn(histQuery, todayQuery);

            // My active CMMN
            when(caseService.countActiveCaseInstances("testuser")).thenReturn(1L);

            DashboardSummaryDTO result = workspaceService.getDashboardSummary();

            assertNotNull(result);
            assertEquals(5L, result.getActiveCount());      // 3 BPMN + 2 CMMN
            assertEquals(15L, result.getCompletedCount());   // 10 BPMN + 5 CMMN
            assertEquals(2L, result.getStartedTodayCount()); // 2 BPMN today
            assertEquals(2L, result.getMyActiveCount());     // 1 BPMN + 1 CMMN
        }
    }
}
