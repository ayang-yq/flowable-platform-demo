package com.flowable.platform.dto;

import java.time.LocalDateTime;

public class InstanceDTO {
    private String id;
    private String definitionId;
    private String definitionKey;
    private String definitionName;
    private DefinitionType type;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long duration;
    private String startedBy;
    private InstanceStatus status;
    private String businessKey;
    private String tenantId;

    public InstanceDTO() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getDefinitionId() { return definitionId; }
    public void setDefinitionId(String definitionId) { this.definitionId = definitionId; }

    public String getDefinitionKey() { return definitionKey; }
    public void setDefinitionKey(String definitionKey) { this.definitionKey = definitionKey; }

    public String getDefinitionName() { return definitionName; }
    public void setDefinitionName(String definitionName) { this.definitionName = definitionName; }

    public DefinitionType getType() { return type; }
    public void setType(DefinitionType type) { this.type = type; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public Long getDuration() { return duration; }
    public void setDuration(Long duration) { this.duration = duration; }

    public String getStartedBy() { return startedBy; }
    public void setStartedBy(String startedBy) { this.startedBy = startedBy; }

    public InstanceStatus getStatus() { return status; }
    public void setStatus(InstanceStatus status) { this.status = status; }

    public String getBusinessKey() { return businessKey; }
    public void setBusinessKey(String businessKey) { this.businessKey = businessKey; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
}
