package com.flowable.platform.dto;

import java.util.List;
import java.util.UUID;

public class UserInfoDTO {
    private UUID userId;
    private String username;
    private String displayName;
    private String email;
    private String tenantCode;
    private String tenantId;
    private List<String> roles;
    private String avatarUrl;

    public UserInfoDTO() {}

    public UserInfoDTO(UUID userId, String username, String displayName, String email, String tenantCode, String tenantId, List<String> roles, String avatarUrl) {
        this.userId = userId;
        this.username = username;
        this.displayName = displayName;
        this.email = email;
        this.tenantCode = tenantCode;
        this.tenantId = tenantId;
        this.roles = roles;
        this.avatarUrl = avatarUrl;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTenantCode() {
        return tenantCode;
    }

    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}
