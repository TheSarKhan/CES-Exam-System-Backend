package com.ces.exam.payload.response;

import java.time.LocalDateTime;
import java.util.List;

public class SessionStartResponse {
    private Long sessionId;
    private Long assignmentId;
    private String examTitle;
    private Integer durationMinutes;
    private LocalDateTime startTime;
    private List<SessionQuestionResponse> questions;

    public SessionStartResponse(Long sessionId, Long assignmentId, String examTitle,
                                Integer durationMinutes, LocalDateTime startTime,
                                List<SessionQuestionResponse> questions) {
        this.sessionId = sessionId;
        this.assignmentId = assignmentId;
        this.examTitle = examTitle;
        this.durationMinutes = durationMinutes;
        this.startTime = startTime;
        this.questions = questions;
    }

    public Long getSessionId() { return sessionId; }
    public Long getAssignmentId() { return assignmentId; }
    public String getExamTitle() { return examTitle; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public LocalDateTime getStartTime() { return startTime; }
    public List<SessionQuestionResponse> getQuestions() { return questions; }
}
