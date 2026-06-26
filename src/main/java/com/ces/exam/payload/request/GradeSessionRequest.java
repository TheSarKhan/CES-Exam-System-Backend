package com.ces.exam.payload.request;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public class GradeSessionRequest {

    @NotNull
    private List<AnswerGrade> grades;

    public List<AnswerGrade> getGrades() { return grades; }
    public void setGrades(List<AnswerGrade> grades) { this.grades = grades; }

    public static class AnswerGrade {
        @NotNull
        private Long questionId;
        @NotNull
        private BigDecimal awardedScore;

        public Long getQuestionId() { return questionId; }
        public void setQuestionId(Long questionId) { this.questionId = questionId; }
        public BigDecimal getAwardedScore() { return awardedScore; }
        public void setAwardedScore(BigDecimal awardedScore) { this.awardedScore = awardedScore; }
    }
}
