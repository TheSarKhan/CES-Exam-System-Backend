package com.ces.exam.payload.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.List;

public class UserRequest {
    @NotBlank
    @Email
    private String email;

    @NotBlank(message = "Parol tələb olunur")
    @Pattern(regexp = ValidationPatterns.PASSWORD, message = ValidationPatterns.PASSWORD_MSG)
    private String password;

    @NotBlank
    @Pattern(regexp = ValidationPatterns.NAME, message = ValidationPatterns.NAME_MSG)
    private String firstName;

    @NotBlank
    @Pattern(regexp = ValidationPatterns.NAME, message = ValidationPatterns.NAME_MSG)
    private String lastName;

    @NotNull(message = "Şöbə seçilməlidir")
    private Long departmentId;

    @NotNull
    private List<Long> roleIds;

    // Getters and Setters
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
}
