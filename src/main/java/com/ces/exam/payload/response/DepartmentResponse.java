package com.ces.exam.payload.response;

import java.time.LocalDateTime;

public class DepartmentResponse {
    private Long id;
    private String name;
    private LocalDateTime createdAt;
    private long memberCount;

    public DepartmentResponse(Long id, String name, LocalDateTime createdAt, long memberCount) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
        this.memberCount = memberCount;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public long getMemberCount() { return memberCount; }
    public void setMemberCount(long memberCount) { this.memberCount = memberCount; }
}
