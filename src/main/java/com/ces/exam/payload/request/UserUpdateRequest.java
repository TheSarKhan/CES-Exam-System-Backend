package com.ces.exam.payload.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class UserUpdateRequest {
    @NotBlank
    @Email
    private String email;

    private String password;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    private Long departmentId;

    @NotNull
    private List<Long> roleIds;

    @NotBlank
    private String status;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
    public List<Long> getRoleIds() { return roleIds; }
    public void setRoleIds(List<Long> roleIds) { this.roleIds = roleIds; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
