package com.ces.exam.payload.response;

import com.ces.exam.model.entity.AuditLog;

import java.time.LocalDateTime;

public class AuditLogResponse {
    private Long id;
    private String userName;
    private String userRole;
    private String module;
    private String action;
    private String httpMethod;
    private String path;
    private Integer statusCode;
    private String ipAddress;
    private LocalDateTime createdAt;

    public AuditLogResponse(AuditLog a) {
        this.id = a.getId();
        this.userName = a.getUserName();
        this.userRole = a.getUserRole();
        this.module = a.getModule();
        this.action = a.getAction();
        this.httpMethod = a.getHttpMethod();
        this.path = a.getPath();
        this.statusCode = a.getStatusCode();
        this.ipAddress = a.getIpAddress();
        this.createdAt = a.getCreatedAt();
    }

    public Long getId() { return id; }
    public String getUserName() { return userName; }
    public String getUserRole() { return userRole; }
    public String getModule() { return module; }
    public String getAction() { return action; }
    public String getHttpMethod() { return httpMethod; }
    public String getPath() { return path; }
    public Integer getStatusCode() { return statusCode; }
    public String getIpAddress() { return ipAddress; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
