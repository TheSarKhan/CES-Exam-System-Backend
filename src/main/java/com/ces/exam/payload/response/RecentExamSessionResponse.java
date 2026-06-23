package com.ces.exam.payload.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RecentExamSessionResponse {
    private Long sessionId;
    private String userName;
    private String examTitle;
    private BigDecimal score;
    private Boolean passed;
    private LocalDateTime completedAt;

    public RecentExamSessionResponse(Long sessionId, String userName, String examTitle,
                                     BigDecimal score, Boolean passed, LocalDateTime completedAt) {
        this.sessionId = sessionId;
        this.userName = userName;
        this.examTitle = examTitle;
        this.score = score;
        this.passed = passed;
        this.completedAt = completedAt;
    }

    public Long getSessionId() { return sessionId; }
    public String getUserName() { return userName; }
    public String getExamTitle() { return examTitle; }
    public BigDecimal getScore() { return score; }
    public Boolean getPassed() { return passed; }
    public LocalDateTime getCompletedAt() { return completedAt; }
}
