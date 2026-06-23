package com.ces.exam.payload.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class DashboardStatsResponse {
    private long totalUsers;
    private long activeExams;
    private long completedThisMonth;
    private List<RecentExamSessionResponse> recentSessions;

    public DashboardStatsResponse(long totalUsers, long activeExams, long completedThisMonth,
                                  List<RecentExamSessionResponse> recentSessions) {
        this.totalUsers = totalUsers;
        this.activeExams = activeExams;
        this.completedThisMonth = completedThisMonth;
        this.recentSessions = recentSessions;
    }

    public long getTotalUsers() { return totalUsers; }
    public long getActiveExams() { return activeExams; }
    public long getCompletedThisMonth() { return completedThisMonth; }
    public List<RecentExamSessionResponse> getRecentSessions() { return recentSessions; }
}
