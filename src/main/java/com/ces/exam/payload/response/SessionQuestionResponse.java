package com.ces.exam.payload.response;

import java.math.BigDecimal;
import java.util.List;

public class SessionQuestionResponse {
    private Long id;
    private String type;
    private String text;
    private String imageUrl;
    private BigDecimal score;
    private List<SessionQuestionOptionResponse> options;

    public SessionQuestionResponse(Long id, String type, String text, String imageUrl, BigDecimal score,
                                   List<SessionQuestionOptionResponse> options) {
        this.id = id;
        this.type = type;
        this.text = text;
        this.imageUrl = imageUrl;
        this.score = score;
        this.options = options;
    }

    public Long getId() { return id; }
    public String getType() { return type; }
    public String getText() { return text; }
    public String getImageUrl() { return imageUrl; }
    public BigDecimal getScore() { return score; }
    public List<SessionQuestionOptionResponse> getOptions() { return options; }
}
