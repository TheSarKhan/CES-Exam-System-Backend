package com.ces.exam.payload.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class SessionAnswerResultResponse {
    private Long questionId;
    private String questionText;
    private String type;
    private Long selectedOptionId;
    private String selectedOptionText;
    private String textAnswer;
    private Boolean isCorrect;
    private BigDecimal score;

    public SessionAnswerResultResponse(Long questionId, String questionText, String type,
                                       Long selectedOptionId, String selectedOptionText,
                                       String textAnswer, Boolean isCorrect, BigDecimal score) {
        this.questionId = questionId;
        this.questionText = questionText;
        this.type = type;
        this.selectedOptionId = selectedOptionId;
        this.selectedOptionText = selectedOptionText;
        this.textAnswer = textAnswer;
        this.isCorrect = isCorrect;
        this.score = score;
    }

    public Long getQuestionId() { return questionId; }
    public String getQuestionText() { return questionText; }
    public String getType() { return type; }
    public Long getSelectedOptionId() { return selectedOptionId; }
    public String getSelectedOptionText() { return selectedOptionText; }
    public String getTextAnswer() { return textAnswer; }
    public Boolean getIsCorrect() { return isCorrect; }
    public BigDecimal getScore() { return score; }
}
