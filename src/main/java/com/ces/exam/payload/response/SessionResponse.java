package com.ces.exam.payload.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class SessionResponse {
    private Long id;
    private Long assignmentId;
    private String examTitle;
    private String examDescription;
    private Integer durationMinutes;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private BigDecimal score;
    private Boolean passed;
    private List<SessionQuestionResponse> questions;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getAssignmentId() { return assignmentId; }
    public void setAssignmentId(Long assignmentId) { this.assignmentId = assignmentId; }
    public String getExamTitle() { return examTitle; }
    public void setExamTitle(String examTitle) { this.examTitle = examTitle; }
    public String getExamDescription() { return examDescription; }
    public void setExamDescription(String examDescription) { this.examDescription = examDescription; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public BigDecimal getScore() { return score; }
    public void setScore(BigDecimal score) { this.score = score; }
    public Boolean getPassed() { return passed; }
    public void setPassed(Boolean passed) { this.passed = passed; }
    public List<SessionQuestionResponse> getQuestions() { return questions; }
    public void setQuestions(List<SessionQuestionResponse> questions) { this.questions = questions; }

    public static class SessionQuestionResponse {
        private Long id;
        private Long questionId;
        private String type;
        private String text;
        private BigDecimal score;
        private List<OptionResponse> options;
        
        // Results (only populated on getSessionResult)
        private Long selectedOptionId;
        private List<Long> selectedOptionIds;
        private String textAnswer;
        private Boolean isCorrect;
        private List<Long> correctOptionIds;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getQuestionId() { return questionId; }
        public void setQuestionId(Long questionId) { this.questionId = questionId; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
        public BigDecimal getScore() { return score; }
        public void setScore(BigDecimal score) { this.score = score; }
        public List<OptionResponse> getOptions() { return options; }
        public void setOptions(List<OptionResponse> options) { this.options = options; }

        public Long getSelectedOptionId() { return selectedOptionId; }
        public void setSelectedOptionId(Long selectedOptionId) { this.selectedOptionId = selectedOptionId; }
        public List<Long> getSelectedOptionIds() { return selectedOptionIds; }
        public void setSelectedOptionIds(List<Long> selectedOptionIds) { this.selectedOptionIds = selectedOptionIds; }
        public String getTextAnswer() { return textAnswer; }
        public void setTextAnswer(String textAnswer) { this.textAnswer = textAnswer; }
        public Boolean getIsCorrect() { return isCorrect; }
        public void setIsCorrect(Boolean isCorrect) { this.isCorrect = isCorrect; }
        public List<Long> getCorrectOptionIds() { return correctOptionIds; }
        public void setCorrectOptionIds(List<Long> correctOptionIds) { this.correctOptionIds = correctOptionIds; }
    }

    public static class OptionResponse {
        private Long id;
        private String text;
        private Integer sortOrder;
        private Boolean isCorrect; // Only populated on getSessionResult

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
        public Integer getSortOrder() { return sortOrder; }
        public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
        public Boolean getIsCorrect() { return isCorrect; }
        public void setIsCorrect(Boolean isCorrect) { this.isCorrect = isCorrect; }
    }
}
