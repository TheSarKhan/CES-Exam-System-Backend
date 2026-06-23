package com.ces.exam.payload.request;

import jakarta.validation.constraints.NotNull;

public class SessionStartRequest {
    @NotNull
    private Long assignmentId;

    public Long getAssignmentId() { return assignmentId; }
    public void setAssignmentId(Long assignmentId) { this.assignmentId = assignmentId; }
}
