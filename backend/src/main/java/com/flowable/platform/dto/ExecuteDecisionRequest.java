package com.flowable.platform.dto;

import java.util.Map;

public class ExecuteDecisionRequest {
    private Map<String, Object> inputVariables;

    public ExecuteDecisionRequest() {}

    public Map<String, Object> getInputVariables() { return inputVariables; }
    public void setInputVariables(Map<String, Object> inputVariables) { this.inputVariables = inputVariables; }
}
