package com.flowable.platform.dto;

import java.time.LocalDateTime;

public class DefinitionDTO {
    private String id;
    private String key;
    private String name;
    private int version;
    private String category;
    private DefinitionType type;
    private boolean hasStartForm;
    private LocalDateTime deploymentTime;

    public DefinitionDTO() {}

    public DefinitionDTO(String id, String key, String name, int version, String category,
                         DefinitionType type, boolean hasStartForm, LocalDateTime deploymentTime) {
        this.id = id;
        this.key = key;
        this.name = name;
        this.version = version;
        this.category = category;
        this.type = type;
        this.hasStartForm = hasStartForm;
        this.deploymentTime = deploymentTime;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public DefinitionType getType() { return type; }
    public void setType(DefinitionType type) { this.type = type; }

    public boolean isHasStartForm() { return hasStartForm; }
    public void setHasStartForm(boolean hasStartForm) { this.hasStartForm = hasStartForm; }

    public LocalDateTime getDeploymentTime() { return deploymentTime; }
    public void setDeploymentTime(LocalDateTime deploymentTime) { this.deploymentTime = deploymentTime; }
}
