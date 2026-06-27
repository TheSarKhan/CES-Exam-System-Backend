package com.ces.exam.payload.response;

import java.time.LocalDateTime;
import java.util.List;

public class SessionStartResponse {
    private Long sessionId;
    private Long assignmentId;
    private String examTitle;
    private Integer durationMinutes;
    private LocalDateTime startTime;
    // Server clock at response time — lets the client correct for local clock skew
    // so the countdown ends at the true deadline regardless of the device's clock.
    private LocalDateTime serverTime;
    private List<SessionQuestionResponse> questions;

    public SessionStartResponse(Long sessionId, Long assignmentId, String examTitle,
                                Integer durationMinutes, LocalDateTime startTime,
                                LocalDateTime serverTime,
                                List<SessionQuestionResponse> questions) {
        this.sessionId = sessionId;
        this.assignmentId = assignmentId;
        this.examTitle = examTitle;
        this.durationMinutes = durationMinutes;
        this.startTime = startTime;
        this.serverTime = serverTime;
        this.questions = questions;
    }

    public Long getSessionId() { return sessionId; }
    public Long getAssignmentId() { return assignmentId; }
    public String getExamTitle() { return examTitle; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getServerTime() { return serverTime; }
    public List<SessionQuestionResponse> getQuestions() { return questions; }
}
