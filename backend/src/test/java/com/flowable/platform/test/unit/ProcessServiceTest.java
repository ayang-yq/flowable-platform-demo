package com.flowable.platform.test.unit;

import com.flowable.platform.dto.DiagramDataDTO;
import com.flowable.platform.service.ProcessService;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskQuery;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricActivityInstanceQuery;
import org.flowable.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.repository.ProcessDefinitionQuery;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ExecutionQuery;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.runtime.ProcessInstanceQuery;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ProcessServiceTest extends AbstractUnitTest {

    @Mock private RuntimeService runtimeService;
    @Mock private RepositoryService repositoryService;
    @Mock private HistoryService historyService;
    @Mock private TaskService taskService;

    @InjectMocks private ProcessService processService;

    @Test
    void getProcessInstanceDiagram_returnsNull_whenInstanceNotFound() {
        ProcessInstanceQuery query = mock(ProcessInstanceQuery.class);
        when(runtimeService.createProcessInstanceQuery()).thenReturn(query);
        when(query.processInstanceId("nonexistent")).thenReturn(query);
        when(query.singleResult()).thenReturn(null);

        DiagramDataDTO result = processService.getProcessInstanceDiagram("nonexistent");

        assertNull(result);
    }

    @Test
    void getProcessInstanceDiagram_returnsDiagramData_forActiveInstance() {
        // Mock process instance
        ProcessInstance instance = mock(ProcessInstance.class);
        when(instance.getId()).thenReturn("proc-1");
        when(instance.getProcessDefinitionId()).thenReturn("simpleProcess:1:1");

        ProcessInstanceQuery instanceQuery = mock(ProcessInstanceQuery.class);
        when(runtimeService.createProcessInstanceQuery()).thenReturn(instanceQuery);
        when(instanceQuery.processInstanceId("proc-1")).thenReturn(instanceQuery);
        when(instanceQuery.singleResult()).thenReturn(instance);

        // Mock process definition
        ProcessDefinition definition = mock(ProcessDefinition.class);
        when(definition.getId()).thenReturn("simpleProcess:1:1");
        when(definition.getResourceName()).thenReturn("processes/simple-process.bpmn");
        when(definition.getDeploymentId()).thenReturn("dep-1");

        ProcessDefinitionQuery defQuery = mock(ProcessDefinitionQuery.class);
        when(repositoryService.createProcessDefinitionQuery()).thenReturn(defQuery);
        when(defQuery.processDefinitionId("simpleProcess:1:1")).thenReturn(defQuery);
        when(defQuery.singleResult()).thenReturn(definition);

        // Mock BPMN resource
        String bpmnXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><definitions xmlns=\"http://www.omg.org/spec/BPMN/20100524/MODEL\"><process id=\"simpleProcess\"><startEvent id=\"start\"/><userTask id=\"task1\" name=\"Review\"/><endEvent id=\"end\"/></process></definitions>";
        when(repositoryService.getResourceAsStream(eq("dep-1"), eq("processes/simple-process.bpmn")))
                .thenReturn(new ByteArrayInputStream(bpmnXml.getBytes(StandardCharsets.UTF_8)));

        // Mock active executions
        ExecutionQuery execQuery = mock(ExecutionQuery.class);
        when(runtimeService.createExecutionQuery()).thenReturn(execQuery);
        when(execQuery.processInstanceId("proc-1")).thenReturn(execQuery);
        when(execQuery.onlyChildExecutions()).thenReturn(execQuery);
        when(execQuery.list()).thenReturn(Collections.emptyList());

        // Mock active tasks
        TaskQuery taskQuery = mock(TaskQuery.class);
        when(taskService.createTaskQuery()).thenReturn(taskQuery);
        when(taskQuery.processInstanceId("proc-1")).thenReturn(taskQuery);
        when(taskQuery.list()).thenReturn(Collections.emptyList());

        // Mock historic activities for completed elements
        HistoricActivityInstanceQuery histQuery = mock(HistoricActivityInstanceQuery.class);
        when(historyService.createHistoricActivityInstanceQuery()).thenReturn(histQuery);
        when(histQuery.processInstanceId("proc-1")).thenReturn(histQuery);
        when(histQuery.finished()).thenReturn(histQuery);
        when(histQuery.orderByHistoricActivityInstanceEndTime()).thenReturn(histQuery);
        when(histQuery.asc()).thenReturn(histQuery);
        when(histQuery.list()).thenReturn(Collections.emptyList());

        DiagramDataDTO result = processService.getProcessInstanceDiagram("proc-1");

        assertNotNull(result);
        assertNotNull(result.getDiagramXml());
        assertTrue(result.getDiagramXml().contains("simpleProcess"));
    }

    @Test
    void getProcessInstanceDiagram_returnsNull_whenDefinitionNotFound() {
        ProcessInstance instance = mock(ProcessInstance.class);
        when(instance.getId()).thenReturn("proc-1");
        when(instance.getProcessDefinitionId()).thenReturn("missing:1:1");

        ProcessInstanceQuery instanceQuery = mock(ProcessInstanceQuery.class);
        when(runtimeService.createProcessInstanceQuery()).thenReturn(instanceQuery);
        when(instanceQuery.processInstanceId("proc-1")).thenReturn(instanceQuery);
        when(instanceQuery.singleResult()).thenReturn(instance);

        ProcessDefinitionQuery defQuery = mock(ProcessDefinitionQuery.class);
        when(repositoryService.createProcessDefinitionQuery()).thenReturn(defQuery);
        when(defQuery.processDefinitionId("missing:1:1")).thenReturn(defQuery);
        when(defQuery.singleResult()).thenReturn(null);

        DiagramDataDTO result = processService.getProcessInstanceDiagram("proc-1");

        assertNull(result);
    }

    @Test
    void getProcessInstanceDiagram_includesActiveTaskStates() {
        ProcessInstance instance = mock(ProcessInstance.class);
        when(instance.getId()).thenReturn("proc-1");
        when(instance.getProcessDefinitionId()).thenReturn("proc:1:1");

        ProcessInstanceQuery instanceQuery = mock(ProcessInstanceQuery.class);
        when(runtimeService.createProcessInstanceQuery()).thenReturn(instanceQuery);
        when(instanceQuery.processInstanceId("proc-1")).thenReturn(instanceQuery);
        when(instanceQuery.singleResult()).thenReturn(instance);

        ProcessDefinition definition = mock(ProcessDefinition.class);
        when(definition.getId()).thenReturn("proc:1:1");
        when(definition.getResourceName()).thenReturn("proc.bpmn");
        when(definition.getDeploymentId()).thenReturn("dep-1");

        ProcessDefinitionQuery defQuery = mock(ProcessDefinitionQuery.class);
        when(repositoryService.createProcessDefinitionQuery()).thenReturn(defQuery);
        when(defQuery.processDefinitionId("proc:1:1")).thenReturn(defQuery);
        when(defQuery.singleResult()).thenReturn(definition);

        String bpmnXml = "<?xml version=\"1.0\"?><definitions xmlns=\"http://www.omg.org/spec/BPMN/20100524/MODEL\"><process id=\"proc\"><startEvent id=\"s\"/><userTask id=\"t1\"/></process></definitions>";
        when(repositoryService.getResourceAsStream(eq("dep-1"), eq("proc.bpmn")))
                .thenReturn(new ByteArrayInputStream(bpmnXml.getBytes(StandardCharsets.UTF_8)));

        // Mock active task
        Task task = mock(Task.class);
        when(task.getTaskDefinitionKey()).thenReturn("t1");
        TaskQuery taskQuery = mock(TaskQuery.class);
        when(taskService.createTaskQuery()).thenReturn(taskQuery);
        when(taskQuery.processInstanceId("proc-1")).thenReturn(taskQuery);
        when(taskQuery.list()).thenReturn(List.of(task));

        ExecutionQuery execQuery = mock(ExecutionQuery.class);
        when(runtimeService.createExecutionQuery()).thenReturn(execQuery);
        when(execQuery.processInstanceId("proc-1")).thenReturn(execQuery);
        when(execQuery.onlyChildExecutions()).thenReturn(execQuery);
        when(execQuery.list()).thenReturn(Collections.emptyList());

        HistoricActivityInstanceQuery histQuery = mock(HistoricActivityInstanceQuery.class);
        when(historyService.createHistoricActivityInstanceQuery()).thenReturn(histQuery);
        when(histQuery.processInstanceId("proc-1")).thenReturn(histQuery);
        when(histQuery.finished()).thenReturn(histQuery);
        when(histQuery.orderByHistoricActivityInstanceEndTime()).thenReturn(histQuery);
        when(histQuery.asc()).thenReturn(histQuery);
        when(histQuery.list()).thenReturn(Collections.emptyList());

        DiagramDataDTO result = processService.getProcessInstanceDiagram("proc-1");

        assertNotNull(result);
        assertEquals("t1", result.getCurrentElementId());
        assertTrue(result.getActiveElementIds().contains("t1"));
    }
}
