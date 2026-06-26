package com.ces.exam.payload.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
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
    private int pendingGrading;       // answers still awaiting manual grading
    private BigDecimal earnedScore;   // total points earned so far
    private BigDecimal maxScore;      // total points available
    private boolean resultHidden;     // true → candidate must not see the score/answers
    private String terminationReason; // why the session ended automatically; null for a normal submission

    public SessionResultResponse(Long sessionId, String examTitle, String status, BigDecimal score,
                                 Boolean passed, BigDecimal passMark, LocalDateTime startTime,
                                 LocalDateTime endTime, List<SessionAnswerResultResponse> answers,
                                 int pendingGrading, BigDecimal earnedScore, BigDecimal maxScore,
                                 String terminationReason) {
        this.sessionId = sessionId;
        this.examTitle = examTitle;
        this.status = status;
        this.score = score;
        this.passed = passed;
        this.passMark = passMark;
        this.startTime = startTime;
        this.endTime = endTime;
        this.answers = answers;
        this.pendingGrading = pendingGrading;
        this.earnedScore = earnedScore;
        this.maxScore = maxScore;
        this.terminationReason = terminationReason;
    }

    /** Candidate-facing masked result — hides score and answers, keeps the status and how it ended. */
    public static SessionResultResponse hidden(Long sessionId, String examTitle, String status,
                                               LocalDateTime startTime, LocalDateTime endTime,
                                               String terminationReason) {
        SessionResultResponse r = new SessionResultResponse(sessionId, examTitle, status, null, null, null,
                startTime, endTime, Collections.emptyList(), 0, null, null, terminationReason);
        r.resultHidden = true;
        return r;
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
    public int getPendingGrading() { return pendingGrading; }
    public BigDecimal getEarnedScore() { return earnedScore; }
    public BigDecimal getMaxScore() { return maxScore; }
    public boolean isResultHidden() { return resultHidden; }
    public String getTerminationReason() { return terminationReason; }
}
