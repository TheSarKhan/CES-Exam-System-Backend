package com.ces.exam.payload.response;

import java.math.BigDecimal;
import java.util.List;

public class DashboardStatsResponse {
    private long totalUsers;
    private long totalExams;
    private long activeExams;
    private long completedThisMonth;
    private long completedTotal;
    private BigDecimal avgScore;   // overall, all completed sessions
    private BigDecimal passRate;   // overall, all completed sessions
    private int pendingGradingCount;
    private int flaggedCount;
    private List<RecentExamSessionResponse> recentSessions;
    private List<AttentionSession> pendingGrading;
    private List<AttentionSession> flaggedSessions;
    private List<DayActivity> weeklyActivity;

    public DashboardStatsResponse(long totalUsers, long totalExams, long activeExams, long completedThisMonth,
                                  long completedTotal, BigDecimal avgScore, BigDecimal passRate,
                                  int pendingGradingCount, int flaggedCount,
                                  List<RecentExamSessionResponse> recentSessions,
                                  List<AttentionSession> pendingGrading, List<AttentionSession> flaggedSessions,
                                  List<DayActivity> weeklyActivity) {
        this.totalUsers = totalUsers;
        this.totalExams = totalExams;
        this.activeExams = activeExams;
        this.completedThisMonth = completedThisMonth;
        this.completedTotal = completedTotal;
        this.avgScore = avgScore;
        this.passRate = passRate;
        this.pendingGradingCount = pendingGradingCount;
        this.flaggedCount = flaggedCount;
        this.recentSessions = recentSessions;
        this.pendingGrading = pendingGrading;
        this.flaggedSessions = flaggedSessions;
        this.weeklyActivity = weeklyActivity;
    }

    public long getTotalUsers() { return totalUsers; }
    public long getTotalExams() { return totalExams; }
    public long getActiveExams() { return activeExams; }
    public long getCompletedThisMonth() { return completedThisMonth; }
    public long getCompletedTotal() { return completedTotal; }
    public BigDecimal getAvgScore() { return avgScore; }
    public BigDecimal getPassRate() { return passRate; }
    public int getPendingGradingCount() { return pendingGradingCount; }
    public int getFlaggedCount() { return flaggedCount; }
    public List<RecentExamSessionResponse> getRecentSessions() { return recentSessions; }
    public List<AttentionSession> getPendingGrading() { return pendingGrading; }
    public List<AttentionSession> getFlaggedSessions() { return flaggedSessions; }
    public List<DayActivity> getWeeklyActivity() { return weeklyActivity; }

    public static class AttentionSession {
        private Long sessionId;
        private String userName;
        private Long examId;
        private String examTitle;
        private int count;

        public AttentionSession(Long sessionId, String userName, Long examId, String examTitle, int count) {
            this.sessionId = sessionId;
            this.userName = userName;
            this.examId = examId;
            this.examTitle = examTitle;
            this.count = count;
        }

        public Long getSessionId() { return sessionId; }
        public String getUserName() { return userName; }
        public Long getExamId() { return examId; }
        public String getExamTitle() { return examTitle; }
        public int getCount() { return count; }
    }

    public static class DayActivity {
        private String label;
        private long count;

        public DayActivity(String label, long count) {
            this.label = label;
            this.count = count;
        }

        public String getLabel() { return label; }
        public long getCount() { return count; }
    }
}
