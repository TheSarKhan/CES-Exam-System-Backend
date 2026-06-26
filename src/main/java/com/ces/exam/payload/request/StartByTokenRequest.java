package com.ces.exam.payload.request;

public class StartByTokenRequest {
    // Optional: the name the taker enters when opening an anonymous link.
    private String candidateName;

    public String getCandidateName() { return candidateName; }
    public void setCandidateName(String candidateName) { this.candidateName = candidateName; }
}
