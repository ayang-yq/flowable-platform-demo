package com.flowable.platform.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class DepartmentDTO {
    private UUID id;
    private String name;
    private String code;
    private UUID parentId;
    private String parentName;
    private UUID managerId;
    private String managerName;
    private String path;
    private Integer level;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private List<DepartmentDTO> children;

    public DepartmentDTO() {}

    // All getters and setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public UUID getParentId() { return parentId; }
    public void setParentId(UUID parentId) { this.parentId = parentId; }
    public String getParentName() { return parentName; }
    public void setParentName(String parentName) { this.parentName = parentName; }
    public UUID getManagerId() { return managerId; }
    public void setManagerId(UUID managerId) { this.managerId = managerId; }
    public String getManagerName() { return managerName; }
    public void setManagerName(String managerName) { this.managerName = managerName; }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public Integer getLevel() { return level; }
    public void setLevel(Integer level) { this.level = level; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public List<DepartmentDTO> getChildren() { return children; }
    public void setChildren(List<DepartmentDTO> children) { this.children = children; }
}
