package com.ces.exam.payload.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class ExamResultsResponse {
    private String examTitle;
    private List<SessionRow> sessions;
    private List<PendingLink> pendingLinks;

    public ExamResultsResponse(String examTitle, List<SessionRow> sessions, List<PendingLink> pendingLinks) {
        this.examTitle = examTitle;
        this.sessions = sessions;
        this.pendingLinks = pendingLinks;
    }

    public String getExamTitle() { return examTitle; }
    public List<SessionRow> getSessions() { return sessions; }
    public List<PendingLink> getPendingLinks() { return pendingLinks; }

    public static class SessionRow {
        private Long sessionId;
        private String userName;
        private String status;
        private BigDecimal score;
        private Boolean passed;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private int pendingGrading;
        private int violationCount;

        public SessionRow(Long sessionId, String userName, String status, BigDecimal score,
                          Boolean passed, LocalDateTime startTime, LocalDateTime endTime,
                          int pendingGrading, int violationCount) {
            this.sessionId = sessionId;
            this.userName = userName;
            this.status = status;
            this.score = score;
            this.passed = passed;
            this.startTime = startTime;
            this.endTime = endTime;
            this.pendingGrading = pendingGrading;
            this.violationCount = violationCount;
        }

        public Long getSessionId() { return sessionId; }
        public String getUserName() { return userName; }
        public String getStatus() { return status; }
        public BigDecimal getScore() { return score; }
        public Boolean getPassed() { return passed; }
        public LocalDateTime getStartTime() { return startTime; }
        public LocalDateTime getEndTime() { return endTime; }
        public int getPendingGrading() { return pendingGrading; }
        public int getViolationCount() { return violationCount; }
    }

    public static class PendingLink {
        private Long assignmentId;
        private String candidateName;
        private String accessToken;
        private LocalDateTime endDate;
        private String recipientEmail;

        public PendingLink(Long assignmentId, String candidateName, String accessToken,
                           LocalDateTime endDate, String recipientEmail) {
            this.assignmentId = assignmentId;
            this.candidateName = candidateName;
            this.accessToken = accessToken;
            this.endDate = endDate;
            this.recipientEmail = recipientEmail;
        }

        public Long getAssignmentId() { return assignmentId; }
        public String getCandidateName() { return candidateName; }
        public String getAccessToken() { return accessToken; }
        public LocalDateTime getEndDate() { return endDate; }
        public String getRecipientEmail() { return recipientEmail; }
    }
}
