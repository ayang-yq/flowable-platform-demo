package com.flowable.platform.dto;

import jakarta.validation.constraints.Email;
import java.util.Set;

public class UpdateUserRequest {
    @Email private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String displayName;
    private String locale;
    private String timezone;
    private Set<String> roleCodes;
    private Set<String> departmentCodes;

    // getters and setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getLocale() { return locale; }
    public void setLocale(String locale) { this.locale = locale; }
    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }
    public Set<String> getRoleCodes() { return roleCodes; }
    public void setRoleCodes(Set<String> roleCodes) { this.roleCodes = roleCodes; }
    public Set<String> getDepartmentCodes() { return departmentCodes; }
    public void setDepartmentCodes(Set<String> departmentCodes) { this.departmentCodes = departmentCodes; }
}
