package com.ces.exam.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "question_options")
public class QuestionOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;

    // Optional image for this choice (IMAGE_CHOICE); a public URL like /api/v1/public/images/{file}.
    @Column(name = "image_url", length = 512)
    private String imageUrl;

    @Column(name = "is_correct")
    private Boolean isCorrect = false;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Question getQuestion() { return question; }
    public void setQuestion(Question question) { this.question = question; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public Boolean getCorrect() { return isCorrect; }
    public Boolean getIsCorrect() { return isCorrect; }
    public void setCorrect(Boolean correct) { isCorrect = correct; }
    public void setIsCorrect(Boolean correct) { isCorrect = correct; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}
