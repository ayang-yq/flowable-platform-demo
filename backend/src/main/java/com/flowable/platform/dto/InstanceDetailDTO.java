package com.flowable.platform.dto;

import java.util.List;
import java.util.Map;

public class InstanceDetailDTO extends InstanceDTO {
    private Map<String, Object> variables;
    private List<String> currentActivities;
    private List<TaskDTO> tasks;

    public InstanceDetailDTO() {}

    public Map<String, Object> getVariables() { return variables; }
    public void setVariables(Map<String, Object> variables) { this.variables = variables; }

    public List<String> getCurrentActivities() { return currentActivities; }
    public void setCurrentActivities(List<String> currentActivities) { this.currentActivities = currentActivities; }

    public List<TaskDTO> getTasks() { return tasks; }
    public void setTasks(List<TaskDTO> tasks) { this.tasks = tasks; }
}
