package com.ces.exam.payload.response;

import java.time.LocalDateTime;

public class ViolationResponse {
    private String type;
    private String label;
    private String severity;
    private LocalDateTime occurredAt;

    public ViolationResponse(String type, String label, String severity, LocalDateTime occurredAt) {
        this.type = type;
        this.label = label;
        this.severity = severity;
        this.occurredAt = occurredAt;
    }

    public String getType() { return type; }
    public String getLabel() { return label; }
    public String getSeverity() { return severity; }
    public LocalDateTime getOccurredAt() { return occurredAt; }
}
