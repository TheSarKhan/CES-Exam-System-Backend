package com.ces.exam.payload.request;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public class SessionSubmitRequest {
    @NotNull
    private List<QuestionAnswerRequest> answers;

    public List<QuestionAnswerRequest> getAnswers() { return answers; }
    public void setAnswers(List<QuestionAnswerRequest> answers) { this.answers = answers; }

    public static class QuestionAnswerRequest {
        @NotNull
        private Long questionId;

        private Long selectedOptionId; // For SINGLE_CHOICE
        private List<Long> selectedOptionIds; // For MULTIPLE_CHOICE
        private String textAnswer; // For TEXT types

        public Long getQuestionId() { return questionId; }
        public void setQuestionId(Long questionId) { this.questionId = questionId; }
        public Long getSelectedOptionId() { return selectedOptionId; }
        public void setSelectedOptionId(Long selectedOptionId) { this.selectedOptionId = selectedOptionId; }
        public List<Long> getSelectedOptionIds() { return selectedOptionIds; }
        public void setSelectedOptionIds(List<Long> selectedOptionIds) { this.selectedOptionIds = selectedOptionIds; }
        public String getTextAnswer() { return textAnswer; }
        public void setTextAnswer(String textAnswer) { this.textAnswer = textAnswer; }
    }
}
