package com.ces.exam.payload.request;

import jakarta.validation.constraints.NotBlank;

public class QuestionOptionRequest {
    @NotBlank
    private String text;
    private String imageUrl;
    private Boolean isCorrect;
    private Integer sortOrder;

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public Boolean getIsCorrect() { return isCorrect; }
    public void setIsCorrect(Boolean correct) { isCorrect = correct; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}
