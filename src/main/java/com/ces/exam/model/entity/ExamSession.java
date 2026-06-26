package com.ces.exam.model.entity;

import com.ces.exam.model.enums.SessionStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "exam_sessions")
public class ExamSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", nullable = false)
    private ExamAssignment assignment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime = LocalDateTime.now();

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private SessionStatus status = SessionStatus.IN_PROGRESS;

    @Column(precision = 5, scale = 2)
    private BigDecimal score;

    private Boolean passed;

    /** Why the session ended automatically; null for a normal submission. e.g. "PROCTORING". */
    @Column(name = "termination_reason", length = 30)
    private String terminationReason;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.Set<ExamSessionQuestion> sessionQuestions = new java.util.LinkedHashSet<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public ExamAssignment getAssignment() { return assignment; }
    public void setAssignment(ExamAssignment assignment) { this.assignment = assignment; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public SessionStatus getStatus() { return status; }
    public void setStatus(SessionStatus status) { this.status = status; }
    public BigDecimal getScore() { return score; }
    public void setScore(BigDecimal score) { this.score = score; }
    public Boolean getPassed() { return passed; }
    public void setPassed(Boolean passed) { this.passed = passed; }
    public String getTerminationReason() { return terminationReason; }
    public void setTerminationReason(String terminationReason) { this.terminationReason = terminationReason; }
    public java.util.Set<ExamSessionQuestion> getSessionQuestions() { return sessionQuestions; }
    public void setSessionQuestions(java.util.Set<ExamSessionQuestion> sessionQuestions) {
        this.sessionQuestions = sessionQuestions;
        if (sessionQuestions != null) {
            sessionQuestions.forEach(sq -> sq.setSession(this));
        }
    }
}
