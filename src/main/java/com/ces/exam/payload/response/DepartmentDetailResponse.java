package com.ces.exam.payload.response;

import java.time.LocalDateTime;
import java.util.List;

public class DepartmentDetailResponse {
    private Long id;
    private String name;
    private LocalDateTime createdAt;
    private long memberCount;
    private long examsCompleted;
    private Integer avgScore;   // department average, %, nullable
    private Integer passRate;   // % of completed exams passed, nullable
    private List<Member> members;

    public DepartmentDetailResponse(Long id, String name, LocalDateTime createdAt, long memberCount,
                                    long examsCompleted, Integer avgScore, Integer passRate, List<Member> members) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
        this.memberCount = memberCount;
        this.examsCompleted = examsCompleted;
        this.avgScore = avgScore;
        this.passRate = passRate;
        this.members = members;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public long getMemberCount() { return memberCount; }
    public long getExamsCompleted() { return examsCompleted; }
    public Integer getAvgScore() { return avgScore; }
    public Integer getPassRate() { return passRate; }
    public List<Member> getMembers() { return members; }

    public static class Member {
        private Long id;
        private String firstName;
        private String lastName;
        private String email;
        private String status;
        private List<String> roles;
        private long completedExams;
        private Integer avgScore;            // nullable
        private LocalDateTime lastActivity;  // nullable

        public Member(Long id, String firstName, String lastName, String email, String status,
                      List<String> roles, long completedExams, Integer avgScore, LocalDateTime lastActivity) {
            this.id = id;
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
            this.status = status;
            this.roles = roles;
            this.completedExams = completedExams;
            this.avgScore = avgScore;
            this.lastActivity = lastActivity;
        }

        public Long getId() { return id; }
        public String getFirstName() { return firstName; }
        public String getLastName() { return lastName; }
        public String getEmail() { return email; }
        public String getStatus() { return status; }
        public List<String> getRoles() { return roles; }
        public long getCompletedExams() { return completedExams; }
        public Integer getAvgScore() { return avgScore; }
        public LocalDateTime getLastActivity() { return lastActivity; }
    }
}
