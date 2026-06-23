package com.ces.exam.payload.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ExamReportResponse {
    private Long sessionId;
    private Long userId;
    private String userName;
    private String userEmail;
    private String departmentName;
    private Long examId;
    private String examTitle;
    private String examType;
    private BigDecimal score;
    private Boolean passed;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public ExamReportResponse(Long sessionId, Long userId, String userName, String userEmail,
                              String departmentName, Long examId, String examTitle, String examType,
                              BigDecimal score, Boolean passed, LocalDateTime startTime, LocalDateTime endTime) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.departmentName = departmentName;
        this.examId = examId;
        this.examTitle = examTitle;
        this.examType = examType;
        this.score = score;
        this.passed = passed;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public Long getSessionId() { return sessionId; }
    public Long getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getUserEmail() { return userEmail; }
    public String getDepartmentName() { return departmentName; }
    public Long getExamId() { return examId; }
    public String getExamTitle() { return examTitle; }
    public String getExamType() { return examType; }
    public BigDecimal getScore() { return score; }
    public Boolean getPassed() { return passed; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
}
