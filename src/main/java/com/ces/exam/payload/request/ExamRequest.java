package com.ces.exam.payload.request;

import com.ces.exam.model.enums.ExamType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

public class ExamRequest {
    @NotBlank
    private String title;
    private String description;
    
    @NotNull
    private ExamType type;
    private BigDecimal passMark;
    private Integer durationMinutes;

    private List<ExamTopicConfigRequest> topicConfigs;

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
    public List<ExamTopicConfigRequest> getTopicConfigs() { return topicConfigs; }
    public void setTopicConfigs(List<ExamTopicConfigRequest> topicConfigs) { this.topicConfigs = topicConfigs; }

    public static class ExamTopicConfigRequest {
        @NotNull
        private Long topicId;
        @NotNull
        private Integer questionCount;

        public Long getTopicId() { return topicId; }
        public void setTopicId(Long topicId) { this.topicId = topicId; }
        public Integer getQuestionCount() { return questionCount; }
        public void setQuestionCount(Integer questionCount) { this.questionCount = questionCount; }
    }
}
