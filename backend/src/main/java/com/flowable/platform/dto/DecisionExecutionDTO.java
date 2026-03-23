package com.flowable.platform.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class DecisionExecutionDTO {
    private String decisionKey;
    private String decisionName;
    private String executionId;
    private Map<String, Object> inputVariables;
    private List<Map<String, Object>> outputVariables;
    private LocalDateTime executionTime;

    public DecisionExecutionDTO() {}

    public String getDecisionKey() { return decisionKey; }
    public void setDecisionKey(String decisionKey) { this.decisionKey = decisionKey; }

    public String getDecisionName() { return decisionName; }
    public void setDecisionName(String decisionName) { this.decisionName = decisionName; }

    public String getExecutionId() { return executionId; }
    public void setExecutionId(String executionId) { this.executionId = executionId; }

    public Map<String, Object> getInputVariables() { return inputVariables; }
    public void setInputVariables(Map<String, Object> inputVariables) { this.inputVariables = inputVariables; }

    public List<Map<String, Object>> getOutputVariables() { return outputVariables; }
    public void setOutputVariables(List<Map<String, Object>> outputVariables) { this.outputVariables = outputVariables; }

    public LocalDateTime getExecutionTime() { return executionTime; }
    public void setExecutionTime(LocalDateTime executionTime) { this.executionTime = executionTime; }
}
