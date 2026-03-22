package com.flowable.platform.dto;

import java.time.LocalDateTime;

public class ActivityItemDTO {
    private String id;
    private String type;
    private String title;
    private LocalDateTime timestamp;
    private String processDefinitionKey;

    public ActivityItemDTO() {}

    public ActivityItemDTO(String id, String type, String title, LocalDateTime timestamp, String processDefinitionKey) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.timestamp = timestamp;
        this.processDefinitionKey = processDefinitionKey;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getProcessDefinitionKey() {
        return processDefinitionKey;
    }

    public void setProcessDefinitionKey(String processDefinitionKey) {
        this.processDefinitionKey = processDefinitionKey;
    }
}
