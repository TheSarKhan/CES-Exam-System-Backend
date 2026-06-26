package com.ces.exam.payload.response;

import java.math.BigDecimal;
import java.util.List;

public class QuestionResponse {
    private Long id;
    private Long topicId;
    private String type;
    private String text;
    private BigDecimal score;
    private String difficulty;
    private Boolean isActive;
    private List<QuestionOptionResponse> options;

    public QuestionResponse(Long id, Long topicId, String type, String text, BigDecimal score,
                            String difficulty, Boolean isActive, List<QuestionOptionResponse> options) {
        this.id = id;
        this.topicId = topicId;
        this.type = type;
        this.text = text;
        this.score = score;
        this.difficulty = difficulty;
        this.isActive = isActive;
        this.options = options;
    }

    public Long getId() { return id; }
    public Long getTopicId() { return topicId; }
    public String getType() { return type; }
    public String getText() { return text; }
    public BigDecimal getScore() { return score; }
    public String getDifficulty() { return difficulty; }
    public Boolean getIsActive() { return isActive; }
    public List<QuestionOptionResponse> getOptions() { return options; }
}
