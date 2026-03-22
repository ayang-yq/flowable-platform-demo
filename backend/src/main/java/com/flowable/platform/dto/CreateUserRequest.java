package com.flowable.platform.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Set;

public class CreateUserRequest {
    @NotBlank @Size(min = 3, max = 100) private String username;
    @NotBlank @Email private String email;
    @NotBlank @Size(min = 8, max = 100) private String password;
    private String firstName;
    private String lastName;
    private String phone;
    private Set<String> roleCodes;
    private Set<String> departmentCodes;

    // getters and setters for all fields
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public Set<String> getRoleCodes() { return roleCodes; }
    public void setRoleCodes(Set<String> roleCodes) { this.roleCodes = roleCodes; }
    public Set<String> getDepartmentCodes() { return departmentCodes; }
    public void setDepartmentCodes(Set<String> departmentCodes) { this.departmentCodes = departmentCodes; }
}
