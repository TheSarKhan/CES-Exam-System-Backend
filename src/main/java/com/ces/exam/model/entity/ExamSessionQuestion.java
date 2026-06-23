package com.ces.exam.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "exam_session_questions")
public class ExamSessionQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private ExamSession session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "selected_option_id")
    private QuestionOption selectedOption;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "exam_session_question_selected_options",
        joinColumns = @JoinColumn(name = "session_question_id"),
        inverseJoinColumns = @JoinColumn(name = "option_id")
    )
    private java.util.Set<QuestionOption> selectedOptions = new java.util.HashSet<>();

    @Column(name = "text_answer", columnDefinition = "TEXT")
    private String textAnswer;

    @Column(name = "is_correct")
    private Boolean isCorrect;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public ExamSession getSession() { return session; }
    public void setSession(ExamSession session) { this.session = session; }
    public Question getQuestion() { return question; }
    public void setQuestion(Question question) { this.question = question; }
    public QuestionOption getSelectedOption() { return selectedOption; }
    public void setSelectedOption(QuestionOption selectedOption) { this.selectedOption = selectedOption; }
    public java.util.Set<QuestionOption> getSelectedOptions() { return selectedOptions; }
    public void setSelectedOptions(java.util.Set<QuestionOption> selectedOptions) { this.selectedOptions = selectedOptions; }
    public String getTextAnswer() { return textAnswer; }
    public void setTextAnswer(String textAnswer) { this.textAnswer = textAnswer; }
    public Boolean getIsCorrect() { return isCorrect; }
    public void setIsCorrect(Boolean isCorrect) { this.isCorrect = isCorrect; }
}
