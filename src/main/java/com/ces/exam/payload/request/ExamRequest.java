package com.ces.exam.payload.request;

import com.ces.exam.model.enums.Difficulty;
import com.ces.exam.model.enums.ExamType;
import com.ces.exam.model.enums.QuestionType;
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

    // Legacy: random draw of questions from bank topics.
    private List<ExamTopicConfigRequest> topicConfigs;

    // Concrete, ordered question list — each item is either a bank reference
    // (questionId set) or an inline-authored question (the other fields set).
    private List<ExamQuestionRequest> questions;

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
    public List<ExamQuestionRequest> getQuestions() { return questions; }
    public void setQuestions(List<ExamQuestionRequest> questions) { this.questions = questions; }

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

    public static class ExamQuestionRequest {
        // When set, reference an existing bank question; otherwise the fields
        // below describe a new inline question owned by this exam.
        private Long questionId;

        private QuestionType type;
        private String text;
        private String imageUrl;
        private BigDecimal score;
        private Difficulty difficulty;
        private List<QuestionOptionRequest> options;

        public Long getQuestionId() { return questionId; }
        public void setQuestionId(Long questionId) { this.questionId = questionId; }
        public QuestionType getType() { return type; }
        public void setType(QuestionType type) { this.type = type; }
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
        public BigDecimal getScore() { return score; }
        public void setScore(BigDecimal score) { this.score = score; }
        public Difficulty getDifficulty() { return difficulty; }
        public void setDifficulty(Difficulty difficulty) { this.difficulty = difficulty; }
        public List<QuestionOptionRequest> getOptions() { return options; }
        public void setOptions(List<QuestionOptionRequest> options) { this.options = options; }
    }
}
