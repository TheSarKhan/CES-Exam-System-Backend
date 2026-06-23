package com.ces.exam.payload.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class SessionResultResponse {
    private Long sessionId;
    private String examTitle;
    private String status;
    private BigDecimal score;
    private Boolean passed;
    private BigDecimal passMark;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<SessionAnswerResultResponse> answers;

    public SessionResultResponse(Long sessionId, String examTitle, String status, BigDecimal score,
                                 Boolean passed, BigDecimal passMark, LocalDateTime startTime,
                                 LocalDateTime endTime, List<SessionAnswerResultResponse> answers) {
        this.sessionId = sessionId;
        this.examTitle = examTitle;
        this.status = status;
        this.score = score;
        this.passed = passed;
        this.passMark = passMark;
        this.startTime = startTime;
        this.endTime = endTime;
        this.answers = answers;
    }

    public Long getSessionId() { return sessionId; }
    public String getExamTitle() { return examTitle; }
    public String getStatus() { return status; }
    public BigDecimal getScore() { return score; }
    public Boolean getPassed() { return passed; }
    public BigDecimal getPassMark() { return passMark; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public List<SessionAnswerResultResponse> getAnswers() { return answers; }
}
