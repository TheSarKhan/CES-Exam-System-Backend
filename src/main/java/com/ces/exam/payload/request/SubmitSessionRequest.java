package com.ces.exam.payload.request;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public class SubmitSessionRequest {
    @NotNull
    private List<SessionAnswerRequest> answers;

    // Proctoring events captured during the session (optional).
    private List<ViolationRequest> violations;

    public List<SessionAnswerRequest> getAnswers() { return answers; }
    public void setAnswers(List<SessionAnswerRequest> answers) { this.answers = answers; }
    public List<ViolationRequest> getViolations() { return violations; }
    public void setViolations(List<ViolationRequest> violations) { this.violations = violations; }

    public static class ViolationRequest {
        private String type;
        private String label;
        private String severity;
        private Long at; // client epoch millis

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }
        public Long getAt() { return at; }
        public void setAt(Long at) { this.at = at; }
    }
}
