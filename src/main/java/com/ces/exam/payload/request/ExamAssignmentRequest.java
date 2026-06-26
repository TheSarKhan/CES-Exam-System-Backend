package com.ces.exam.payload.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

public class ExamAssignmentRequest {
    @NotNull
    private Long examId;

    // Delivery mode: "INTERNAL" (platform user/department, dashboard) or "LINK" (shareable token link).
    private String mode;

    private Long userId; // Optional if assigning to department
    private Long departmentId; // Optional if assigning to user

    // Internal bulk delivery: assign to many platform users and/or departments at once.
    private List<Long> userIds;
    private List<Long> departmentIds;

    // For LINK mode to an external candidate without a platform account.
    private String candidateName;

    // Optional: when set on a LINK assignment, the invite is e-mailed to this address.
    private String recipientEmail;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    public Long getExamId() { return examId; }
    public void setExamId(Long examId) { this.examId = examId; }
    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
    public List<Long> getUserIds() { return userIds; }
    public void setUserIds(List<Long> userIds) { this.userIds = userIds; }
    public List<Long> getDepartmentIds() { return departmentIds; }
    public void setDepartmentIds(List<Long> departmentIds) { this.departmentIds = departmentIds; }
    public String getCandidateName() { return candidateName; }
    public void setCandidateName(String candidateName) { this.candidateName = candidateName; }
    public String getRecipientEmail() { return recipientEmail; }
    public void setRecipientEmail(String recipientEmail) { this.recipientEmail = recipientEmail; }
    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }
    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }
}
