package com.ces.exam.payload.request;

import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Bulk question import into one topic. The inner question list is intentionally
 * NOT cascade-validated (@Valid) — each row is validated individually in the
 * service so a single bad row can be reported instead of failing the whole call.
 */
public class BulkQuestionRequest {
    @NotNull
    private Long topicId;

    @NotNull
    private List<QuestionRequest> questions;

    public Long getTopicId() { return topicId; }
    public void setTopicId(Long topicId) { this.topicId = topicId; }
    public List<QuestionRequest> getQuestions() { return questions; }
    public void setQuestions(List<QuestionRequest> questions) { this.questions = questions; }
}
