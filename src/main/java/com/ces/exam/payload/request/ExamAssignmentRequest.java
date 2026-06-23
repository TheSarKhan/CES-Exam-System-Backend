package com.ces.exam.payload.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class ExamAssignmentRequest {
    @NotNull
    private Long examId;
    
    private Long userId; // Optional if assigning to department
    private Long departmentId; // Optional if assigning to user

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    public Long getExamId() { return examId; }
    public void setExamId(Long examId) { this.examId = examId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }
    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }
}
