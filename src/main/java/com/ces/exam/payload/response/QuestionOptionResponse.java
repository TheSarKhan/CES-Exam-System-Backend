package com.ces.exam.payload.response;

public class QuestionOptionResponse {
    private Long id;
    private String text;
    private Boolean isCorrect;
    private Integer sortOrder;

    public QuestionOptionResponse(Long id, String text, Boolean isCorrect, Integer sortOrder) {
        this.id = id;
        this.text = text;
        this.isCorrect = isCorrect;
        this.sortOrder = sortOrder;
    }

    public Long getId() { return id; }
    public String getText() { return text; }
    public Boolean getIsCorrect() { return isCorrect; }
    public Integer getSortOrder() { return sortOrder; }
}
