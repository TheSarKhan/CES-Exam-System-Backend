package com.ces.exam.payload.response;

public class TopicResponse {
    private Long id;
    private Long categoryId;
    private String name;

    public TopicResponse(Long id, Long categoryId, String name) {
        this.id = id;
        this.categoryId = categoryId;
        this.name = name;
    }

    public Long getId() { return id; }
    public Long getCategoryId() { return categoryId; }
    public String getName() { return name; }
}
