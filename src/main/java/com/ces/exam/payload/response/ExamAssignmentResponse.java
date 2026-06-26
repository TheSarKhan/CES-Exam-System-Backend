package com.ces.exam.payload.response;

public class ExamAssignmentResponse {
    private Long assignmentId;
    private String accessToken;
    private String candidateName;
    private String examTitle;
    private String recipientEmail;
    private Boolean emailSent;   // null = no e-mail attempted; true/false = delivery outcome

    public ExamAssignmentResponse(Long assignmentId, String accessToken, String candidateName, String examTitle,
                                  String recipientEmail, Boolean emailSent) {
        this.assignmentId = assignmentId;
        this.accessToken = accessToken;
        this.candidateName = candidateName;
        this.examTitle = examTitle;
        this.recipientEmail = recipientEmail;
        this.emailSent = emailSent;
    }

    public Long getAssignmentId() { return assignmentId; }
    public String getAccessToken() { return accessToken; }
    public String getCandidateName() { return candidateName; }
    public String getExamTitle() { return examTitle; }
    public String getRecipientEmail() { return recipientEmail; }
    public Boolean getEmailSent() { return emailSent; }
}
