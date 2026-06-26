package com.ces.exam.payload.response;

import java.time.LocalDateTime;
import java.util.List;

public class AccountResponse {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String departmentName;
    private List<String> roles;
    private LocalDateTime memberSince;
    private Stats stats;

    public AccountResponse(Long id, String email, String firstName, String lastName,
                           String departmentName, List<String> roles, LocalDateTime memberSince,
                           Stats stats) {
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.departmentName = departmentName;
        this.roles = roles;
        this.memberSince = memberSince;
        this.stats = stats;
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getDepartmentName() { return departmentName; }
    public List<String> getRoles() { return roles; }
    public LocalDateTime getMemberSince() { return memberSince; }
    public Stats getStats() { return stats; }

    public static class Stats {
        private int assigned;
        private int completed;
        private int pending;
        private Integer avgScore;   // percent, null if no graded exam yet
        private int passed;
        private Integer bestScore;  // percent, null if no graded exam yet

        public Stats(int assigned, int completed, int pending, Integer avgScore, int passed, Integer bestScore) {
            this.assigned = assigned;
            this.completed = completed;
            this.pending = pending;
            this.avgScore = avgScore;
            this.passed = passed;
            this.bestScore = bestScore;
        }

        public int getAssigned() { return assigned; }
        public int getCompleted() { return completed; }
        public int getPending() { return pending; }
        public Integer getAvgScore() { return avgScore; }
        public int getPassed() { return passed; }
        public Integer getBestScore() { return bestScore; }
    }
}
