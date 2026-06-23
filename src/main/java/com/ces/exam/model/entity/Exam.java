package com.ces.exam.model.entity;

import com.ces.exam.model.enums.ExamType;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "exams")
public class Exam {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ExamType type;

    @Column(name = "pass_mark", precision = 5, scale = 2)
    private BigDecimal passMark;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @OneToMany(mappedBy = "exam", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExamTopicConfig> topicConfigs;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public ExamType getType() { return type; }
    public void setType(ExamType type) { this.type = type; }
    public BigDecimal getPassMark() { return passMark; }
    public void setPassMark(BigDecimal passMark) { this.passMark = passMark; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
    public List<ExamTopicConfig> getTopicConfigs() { return topicConfigs; }
    public void setTopicConfigs(List<ExamTopicConfig> topicConfigs) {
        this.topicConfigs = topicConfigs;
        if(topicConfigs != null) {
            topicConfigs.forEach(tc -> tc.setExam(this));
        }
    }
}
