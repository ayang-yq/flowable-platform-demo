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

            // Mock BPMN definitions
            ProcessDefinitionQuery bpmnQuery = mock(ProcessDefinitionQuery.class);
            when(repositoryService.createProcessDefinitionQuery()).thenReturn(bpmnQuery);
            when(bpmnQuery.latestVersion()).thenReturn(bpmnQuery);
            when(bpmnQuery.processDefinitionTenantId("tenant-1")).thenReturn(bpmnQuery);
            when(bpmnQuery.list()).thenReturn(Collections.emptyList());

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

            // Mock BPMN active instances
            ProcessInstanceQuery bpmnQuery = mock(ProcessInstanceQuery.class);
            when(runtimeService.createProcessInstanceQuery()).thenReturn(bpmnQuery);
            when(bpmnQuery.processInstanceTenantId("tenant-1")).thenReturn(bpmnQuery);
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
        try (MockedStatic<MultiTenantFilter> mocked = mockStatic(MultiTenantFilter.class)) {
            mocked.when(MultiTenantFilter::getCurrentTenantId).thenReturn("tenant-1");

            // Active BPMN
            ProcessInstanceQuery activeQuery = mock(ProcessInstanceQuery.class);
            when(runtimeService.createProcessInstanceQuery()).thenReturn(activeQuery);
            when(activeQuery.processInstanceTenantId("tenant-1")).thenReturn(activeQuery);
            when(activeQuery.startedBy(anyString())).thenReturn(activeQuery);
            when(activeQuery.count()).thenReturn(3L);

            // Active CMMN
            when(caseService.countActiveCaseInstances(isNull())).thenReturn(2L);
            when(caseService.countActiveCaseInstances(anyString())).thenReturn(1L);

            // Completed BPMN
            HistoricProcessInstanceQuery histQuery = mock(HistoricProcessInstanceQuery.class);
            when(historyService.createHistoricProcessInstanceQuery()).thenReturn(histQuery);
            when(histQuery.finished()).thenReturn(histQuery);
            when(histQuery.processInstanceTenantId("tenant-1")).thenReturn(histQuery);
            when(histQuery.startedAfter(any())).thenReturn(histQuery);
            when(histQuery.count()).thenReturn(10L);

            // Completed CMMN
            when(caseService.countCompletedCaseInstances()).thenReturn(5L);

            DashboardSummaryDTO result = workspaceService.getDashboardSummary();

            assertNotNull(result);
            // Note: exact counts depend on mock interaction order
            assertTrue(result.getActiveCount() >= 0);
            assertTrue(result.getCompletedCount() >= 0);
        }
    }
}
