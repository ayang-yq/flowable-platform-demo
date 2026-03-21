package com.flowable.platform.service;

import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.engine.HistoryService;
import org.flowable.engine.ManagementService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.repository.ProcessDefinition;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProcessDiagramService {

    private final RepositoryService repositoryService;
    private final RuntimeService runtimeService;
    private final HistoryService historyService;
    private final ManagementService managementService;

    public ProcessDiagramService(RepositoryService repositoryService,
                                RuntimeService runtimeService,
                                HistoryService historyService,
                                ManagementService managementService) {
        this.repositoryService = repositoryService;
        this.runtimeService = runtimeService;
        this.historyService = historyService;
        this.managementService = managementService;
    }

    /**
     * Get process diagram data for a given process instance
     * Returns process definition XML and current node information
     */
    public Map<String, Object> getProcessDiagramData(String processInstanceId) {
        // Get process instance
        org.flowable.engine.runtime.ProcessInstance processInstance =
                runtimeService.createProcessInstanceQuery()
                        .processInstanceId(processInstanceId)
                        .singleResult();

        if (processInstance == null) {
            throw new IllegalArgumentException("Process instance not found: " + processInstanceId);
        }

        // Get process definition
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(processInstance.getProcessDefinitionId())
                .singleResult();

        // Get BPMN model
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinition.getId());

        // Get current active nodes
        List<String> activeActivityIds = getActiveActivityIds(processInstanceId);

        // Get completed nodes for historical context
        List<String> completedActivityIds = getCompletedActivityIds(processInstanceId);

        Map<String, Object> diagramData = new HashMap<>();
        diagramData.put("processDefinitionId", processDefinition.getId());
        diagramData.put("processDefinitionKey", processDefinition.getKey());
        diagramData.put("activeNodes", activeActivityIds);
        diagramData.put("completedNodes", completedActivityIds);
        diagramData.put("bpmnXml", getProcessDefinitionXml(processDefinition.getId()));

        return diagramData;
    }

    /**
     * Get diagram as SVG string for embedding in HTML
     * Note: Returns a placeholder that directs to frontend rendering
     */
    public String getProcessDiagramSvg(String processInstanceId) {
        // For now, return instructions to use bpmn.js on frontend
        // This is actually the preferred approach for modern web applications
        return String.format("""
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 300">
                <rect width="800" height="300" fill="#f9fafb"/>
                <text x="400" y="150" text-anchor="middle" font-family="Arial, sans-serif" font-size="16" fill="#374151">
                    Process Diagram
                </text>
                <text x="400" y="180" text-anchor="middle" font-family="Arial, sans-serif" font-size="14" fill="#6b7280">
                    Instance: %s
                </text>
                <text x="400" y="210" text-anchor="middle" font-family="Arial, sans-serif" font-size="12" fill="#9ca3af">
                    Interactive diagram rendered by bpmn.js
                </text>
            </svg>
            """, processInstanceId.substring(0, Math.min(20, processInstanceId.length())));
    }

    /**
     * Get active activity IDs for a process instance
     */
    private List<String> getActiveActivityIds(String processInstanceId) {
        // Get current execution activities
        List<String> executionActivityIds = runtimeService.createExecutionQuery()
                .processInstanceId(processInstanceId)
                .list()
                .stream()
                .map(org.flowable.engine.runtime.Execution::getActivityId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // Get active tasks
        List<String> taskActivityIds = runtimeService.createActivityInstanceQuery()
                .processInstanceId(processInstanceId)
                .activityType("userTask")
                .list()
                .stream()
                .map(org.flowable.engine.runtime.ActivityInstance::getActivityId)
                .collect(Collectors.toList());

        // Combine both lists
        Set<String> activeIds = new HashSet<>();
        activeIds.addAll(executionActivityIds);
        activeIds.addAll(taskActivityIds);

        return new ArrayList<>(activeIds);
    }

    /**
     * Get completed activity IDs for a process instance
     */
    private List<String> getCompletedActivityIds(String processInstanceId) {
        List<HistoricActivityInstance> historicActivities = historyService
                .createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceId)
                .finished()
                .list();

        return historicActivities.stream()
                .map(HistoricActivityInstance::getActivityId)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Get process definition XML as string
     */
    public String getProcessDefinitionXml(String processDefinitionId) {
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(processDefinitionId)
                .singleResult();

        if (processDefinition == null) {
            throw new IllegalArgumentException("Process definition not found: " + processDefinitionId);
        }

        // Get BPMN model
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinition.getId());

        // Convert to XML
        BpmnXMLConverter xmlConverter = new BpmnXMLConverter();
        byte[] xmlBytes = xmlConverter.convertToXML(bpmnModel);

        return new String(xmlBytes);
    }

    /**
     * Get all nodes in a process definition
     */
    public List<Map<String, Object>> getProcessNodes(String processDefinitionId) {
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(processDefinitionId)
                .singleResult();

        if (processDefinition == null) {
            throw new IllegalArgumentException("Process definition not found: " + processDefinitionId);
        }

        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinition.getId());

        List<Map<String, Object>> nodes = new ArrayList<>();

        for (org.flowable.bpmn.model.Process process : bpmnModel.getProcesses()) {
            for (org.flowable.bpmn.model.FlowElement flowElement : process.getFlowElements()) {
                if (flowElement instanceof org.flowable.bpmn.model.FlowNode) {
                    org.flowable.bpmn.model.FlowNode flowNode = (org.flowable.bpmn.model.FlowNode) flowElement;
                    Map<String, Object> nodeInfo = new HashMap<>();
                    nodeInfo.put("id", flowNode.getId());
                    nodeInfo.put("name", flowNode.getName());
                    nodeInfo.put("type", flowNode.getClass().getSimpleName());

                    if (flowElement instanceof org.flowable.bpmn.model.UserTask) {
                        org.flowable.bpmn.model.UserTask userTask = (org.flowable.bpmn.model.UserTask) flowElement;
                        nodeInfo.put("assignee", userTask.getAssignee());
                        nodeInfo.put("candidateUsers", userTask.getCandidateUsers());
                        nodeInfo.put("candidateGroups", userTask.getCandidateGroups());
                    }

                    nodes.add(nodeInfo);
                }
            }
        }

        return nodes;
    }
}
