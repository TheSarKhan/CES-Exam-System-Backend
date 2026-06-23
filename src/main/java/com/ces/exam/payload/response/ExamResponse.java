package com.ces.exam.payload.response;

import java.math.BigDecimal;
import java.util.List;

public class ExamResponse {
    private Long id;
    private String title;
    private String type;
    private BigDecimal passMark;
    private Integer durationMinutes;
    private List<ExamTopicConfigResponse> topicConfigs;

    // Constructors, Getters, Setters
    public ExamResponse(Long id, String title, String type, BigDecimal passMark, Integer durationMinutes, List<ExamTopicConfigResponse> topicConfigs) {
        this.id = id;
        this.title = title;
        this.type = type;
        this.passMark = passMark;
        this.durationMinutes = durationMinutes;
        this.topicConfigs = topicConfigs;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getType() { return type; }
    public BigDecimal getPassMark() { return passMark; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public List<ExamTopicConfigResponse> getTopicConfigs() { return topicConfigs; }

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
}
