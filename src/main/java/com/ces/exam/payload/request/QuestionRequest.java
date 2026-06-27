package com.ces.exam.payload.request;

import com.ces.exam.model.enums.Difficulty;
import com.ces.exam.model.enums.QuestionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

public class QuestionRequest {
    @NotNull
    private Long topicId;

    @NotNull
    private QuestionType type;

    @NotBlank
    private String text;

    private String imageUrl;

    private BigDecimal score;

    private Difficulty difficulty;

    private List<QuestionOptionRequest> options;

    public Long getTopicId() { return topicId; }
    public void setTopicId(Long topicId) { this.topicId = topicId; }
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
