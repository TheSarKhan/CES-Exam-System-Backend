package com.ces.exam.payload.response;

import java.math.BigDecimal;

public class SessionAnswerResultResponse {
    private Long questionId;
    private String questionText;
    private String type;
    private Long selectedOptionId;
    private String selectedOptionText;
    private String textAnswer;
    private Boolean isCorrect;
    private BigDecimal score;          // max points for the question
    private BigDecimal awardedScore;   // points actually earned (null while pending)
    private boolean needsGrading;      // true → open-ended answer awaiting manual grading

    public SessionAnswerResultResponse(Long questionId, String questionText, String type,
                                       Long selectedOptionId, String selectedOptionText,
                                       String textAnswer, Boolean isCorrect, BigDecimal score,
                                       BigDecimal awardedScore, boolean needsGrading) {
        this.questionId = questionId;
        this.questionText = questionText;
        this.type = type;
        this.selectedOptionId = selectedOptionId;
        this.selectedOptionText = selectedOptionText;
        this.textAnswer = textAnswer;
        this.isCorrect = isCorrect;
        this.score = score;
        this.awardedScore = awardedScore;
        this.needsGrading = needsGrading;
    }

    public Long getQuestionId() { return questionId; }
    public String getQuestionText() { return questionText; }
    public String getType() { return type; }
    public Long getSelectedOptionId() { return selectedOptionId; }
    public String getSelectedOptionText() { return selectedOptionText; }
    public String getTextAnswer() { return textAnswer; }
    public Boolean getIsCorrect() { return isCorrect; }
    public BigDecimal getScore() { return score; }
    public BigDecimal getAwardedScore() { return awardedScore; }
    public boolean isNeedsGrading() { return needsGrading; }
}
