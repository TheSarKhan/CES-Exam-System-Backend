package com.ces.exam.payload.response;

import java.time.LocalDateTime;

public class TokenAssignmentResponse {
    private String examTitle;
    private String examDescription;
    private String candidateName;
    private Integer durationMinutes;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String status;
    private Long sessionId;

    public TokenAssignmentResponse(String examTitle, String examDescription, String candidateName,
                                   Integer durationMinutes, LocalDateTime startDate, LocalDateTime endDate,
                                   String status, Long sessionId) {
        this.examTitle = examTitle;
        this.examDescription = examDescription;
        this.candidateName = candidateName;
        this.durationMinutes = durationMinutes;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.sessionId = sessionId;
    }

    public String getExamTitle() { return examTitle; }
    public String getExamDescription() { return examDescription; }
    public String getCandidateName() { return candidateName; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public LocalDateTime getStartDate() { return startDate; }
    public LocalDateTime getEndDate() { return endDate; }
    public String getStatus() { return status; }
    public Long getSessionId() { return sessionId; }
}
