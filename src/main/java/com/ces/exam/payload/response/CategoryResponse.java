package com.ces.exam.payload.response;

public class CategoryResponse {
    private Long id;
    private Long departmentId;
    private String departmentName;
    private String name;
    private String description;
    private long topicCount;
    private long questionCount;

    public CategoryResponse(Long id, Long departmentId, String departmentName, String name, String description,
                            long topicCount, long questionCount) {
        this.id = id;
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.name = name;
        this.description = description;
        this.topicCount = topicCount;
        this.questionCount = questionCount;
    }

    public Long getId() { return id; }
    public Long getDepartmentId() { return departmentId; }
    public String getDepartmentName() { return departmentName; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public long getTopicCount() { return topicCount; }
    public long getQuestionCount() { return questionCount; }
}
