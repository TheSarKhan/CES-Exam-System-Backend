package com.ces.exam.payload.response;

public class QuestionOptionResponse {
    private Long id;
    private String text;
    private String imageUrl;
    private Boolean isCorrect;
    private Integer sortOrder;

    public QuestionOptionResponse(Long id, String text, String imageUrl, Boolean isCorrect, Integer sortOrder) {
        this.id = id;
        this.text = text;
        this.imageUrl = imageUrl;
        this.isCorrect = isCorrect;
        this.sortOrder = sortOrder;
    }

    public Long getId() { return id; }
    public String getText() { return text; }
    public String getImageUrl() { return imageUrl; }
    public Boolean getIsCorrect() { return isCorrect; }
    public Integer getSortOrder() { return sortOrder; }
}
