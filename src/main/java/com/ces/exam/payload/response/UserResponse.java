package com.ces.exam.payload.response;

import java.util.List;

public class UserResponse {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private Long departmentId;
    private String departmentName;
    private String status;
    private List<RoleDto> roles;

    public UserResponse(Long id, String email, String firstName, String lastName, Long departmentId, String departmentName, String status, List<RoleDto> roles) {
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.status = status;
        this.roles = roles;
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public Long getDepartmentId() { return departmentId; }
    public String getDepartmentName() { return departmentName; }
    public String getStatus() { return status; }
    public List<RoleDto> getRoles() { return roles; }

    public static class RoleDto {
        private Long id;
        private String name;

        public RoleDto(Long id, String name) {
            this.id = id;
            this.name = name;
        }
        public Long getId() { return id; }
        public String getName() { return name; }
    }
}
