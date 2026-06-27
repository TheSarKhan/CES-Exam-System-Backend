package com.ces.exam.payload.response;

import java.math.BigDecimal;
import java.util.List;

public class ExamResponse {
    private Long id;
    private String title;
    private String description;
    private String type;
    private BigDecimal passMark;
    private Integer durationMinutes;
    private Integer questionCount;
    private ExamStats stats;
    private List<ExamTopicConfigResponse> topicConfigs;
    private List<ExamQuestionResponse> questions;

    public ExamResponse(Long id, String title, String description, String type, BigDecimal passMark,
                        Integer durationMinutes, Integer questionCount,
                        List<ExamTopicConfigResponse> topicConfigs, List<ExamQuestionResponse> questions) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.type = type;
        this.passMark = passMark;
        this.durationMinutes = durationMinutes;
        this.questionCount = questionCount;
        this.topicConfigs = topicConfigs;
        this.questions = questions;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getType() { return type; }
    public BigDecimal getPassMark() { return passMark; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public Integer getQuestionCount() { return questionCount; }
    public ExamStats getStats() { return stats; }
    public void setStats(ExamStats stats) { this.stats = stats; }
    public List<ExamTopicConfigResponse> getTopicConfigs() { return topicConfigs; }
    public List<ExamQuestionResponse> getQuestions() { return questions; }

    public static class ExamStats {
        private int assigned;
        private int completed;
        private int inProgress;
        private Integer avgScore;   // null for surveys or when nobody has finished
        private Integer passRate;   // null for surveys or when nobody has finished

        public ExamStats(int assigned, int completed, int inProgress, Integer avgScore, Integer passRate) {
            this.assigned = assigned;
            this.completed = completed;
            this.inProgress = inProgress;
            this.avgScore = avgScore;
            this.passRate = passRate;
        }

        public int getAssigned() { return assigned; }
        public int getCompleted() { return completed; }
        public int getInProgress() { return inProgress; }
        public Integer getAvgScore() { return avgScore; }
        public Integer getPassRate() { return passRate; }
    }

    public static class ExamTopicConfigResponse {
        private Long topicId;
        private String topicName;
        private Integer questionCount;

        public ExamTopicConfigResponse(Long topicId, String topicName, Integer questionCount) {
            this.topicId = topicId;
            this.topicName = topicName;
            this.questionCount = questionCount;
        }

        public Long getTopicId() { return topicId; }
        public String getTopicName() { return topicName; }
        public Integer getQuestionCount() { return questionCount; }
    }

    public static class ExamQuestionResponse {
        private Long questionId;
        private String type;
        private String text;
        private String imageUrl;
        private BigDecimal score;
        private String difficulty;
        private boolean fromBank;
        private List<QuestionOptionResponse> options;

        public ExamQuestionResponse(Long questionId, String type, String text, String imageUrl, BigDecimal score,
                                    String difficulty, boolean fromBank, List<QuestionOptionResponse> options) {
            this.questionId = questionId;
            this.type = type;
            this.text = text;
            this.imageUrl = imageUrl;
            this.score = score;
            this.difficulty = difficulty;
            this.fromBank = fromBank;
            this.options = options;
        }

        public Long getQuestionId() { return questionId; }
        public String getType() { return type; }
        public String getText() { return text; }
        public String getImageUrl() { return imageUrl; }
        public BigDecimal getScore() { return score; }
        public String getDifficulty() { return difficulty; }
        public boolean isFromBank() { return fromBank; }
        public List<QuestionOptionResponse> getOptions() { return options; }
    }
}
