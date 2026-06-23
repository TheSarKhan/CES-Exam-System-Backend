package com.ces.exam.payload.response;

public class ExamAssignmentResponse {
    private Long assignmentId;
    private String accessToken;
    private String candidateName;
    private String examTitle;

    public ExamAssignmentResponse(Long assignmentId, String accessToken, String candidateName, String examTitle) {
        this.assignmentId = assignmentId;
        this.accessToken = accessToken;
        this.candidateName = candidateName;
        this.examTitle = examTitle;
    }

    public Long getAssignmentId() { return assignmentId; }
    public String getAccessToken() { return accessToken; }
    public String getCandidateName() { return candidateName; }
    public String getExamTitle() { return examTitle; }
}
