package com.ces.exam.payload.response;

import java.math.BigDecimal;
import java.util.List;

public class SessionQuestionOptionResponse {
    private Long id;
    private String text;
    private Integer sortOrder;

    public SessionQuestionOptionResponse(Long id, String text, Integer sortOrder) {
        this.id = id;
        this.text = text;
        this.sortOrder = sortOrder;
    }

    public Long getId() { return id; }
    public String getText() { return text; }
    public Integer getSortOrder() { return sortOrder; }
}
