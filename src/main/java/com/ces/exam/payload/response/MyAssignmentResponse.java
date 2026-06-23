package com.ces.exam.payload.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class MyAssignmentResponse {
    private Long assignmentId;
    private Long examId;
    private String examTitle;
    private String examType;
    private Integer durationMinutes;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String status;
    private Long sessionId;
    private BigDecimal score;
    private Boolean passed;

    public MyAssignmentResponse(Long assignmentId, Long examId, String examTitle, String examType,
                                Integer durationMinutes, LocalDateTime startDate, LocalDateTime endDate,
                                String status, Long sessionId, BigDecimal score, Boolean passed) {
        this.assignmentId = assignmentId;
        this.examId = examId;
        this.examTitle = examTitle;
        this.examType = examType;
        this.durationMinutes = durationMinutes;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.sessionId = sessionId;
        this.score = score;
        this.passed = passed;
    }

    public Long getAssignmentId() { return assignmentId; }
    public Long getExamId() { return examId; }
    public String getExamTitle() { return examTitle; }
    public String getExamType() { return examType; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public LocalDateTime getStartDate() { return startDate; }
    public LocalDateTime getEndDate() { return endDate; }
    public String getStatus() { return status; }
    public Long getSessionId() { return sessionId; }
    public BigDecimal getScore() { return score; }
    public Boolean getPassed() { return passed; }
}
