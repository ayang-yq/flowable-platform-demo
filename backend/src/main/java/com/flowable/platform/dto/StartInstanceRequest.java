package com.flowable.platform.dto;

import java.util.Map;

public class StartInstanceRequest {
    private Map<String, Object> variables;
    private String businessKey;

    public StartInstanceRequest() {}

    public Map<String, Object> getVariables() { return variables; }
    public void setVariables(Map<String, Object> variables) { this.variables = variables; }

    public String getBusinessKey() { return businessKey; }
    public void setBusinessKey(String businessKey) { this.businessKey = businessKey; }
}
