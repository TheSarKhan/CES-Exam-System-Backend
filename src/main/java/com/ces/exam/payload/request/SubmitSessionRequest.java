package com.ces.exam.payload.request;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public class SubmitSessionRequest {
    @NotNull
    private List<SessionAnswerRequest> answers;

    public List<SessionAnswerRequest> getAnswers() { return answers; }
    public void setAnswers(List<SessionAnswerRequest> answers) { this.answers = answers; }
}
