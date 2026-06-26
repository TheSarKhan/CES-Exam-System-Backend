package com.ces.exam.payload.response;

import java.math.BigDecimal;
import java.util.List;

public class ExamAnalyticsResponse {
    private String examTitle;
    private int completedCount;
    private BigDecimal avgScore;
    private BigDecimal passRate;
    private BigDecimal passMark;
    private List<ScoreBucket> scoreDistribution;
    private List<QuestionStat> questionStats;
    private List<DifficultyStat> difficultyStats;
    private List<DepartmentStat> departmentStats;

    public ExamAnalyticsResponse(String examTitle, int completedCount, BigDecimal avgScore, BigDecimal passRate,
                                 BigDecimal passMark, List<ScoreBucket> scoreDistribution, List<QuestionStat> questionStats,
                                 List<DifficultyStat> difficultyStats, List<DepartmentStat> departmentStats) {
        this.examTitle = examTitle;
        this.completedCount = completedCount;
        this.avgScore = avgScore;
        this.passRate = passRate;
        this.passMark = passMark;
        this.scoreDistribution = scoreDistribution;
        this.questionStats = questionStats;
        this.difficultyStats = difficultyStats;
        this.departmentStats = departmentStats;
    }

    public String getExamTitle() { return examTitle; }
    public int getCompletedCount() { return completedCount; }
    public BigDecimal getAvgScore() { return avgScore; }
    public BigDecimal getPassRate() { return passRate; }
    public BigDecimal getPassMark() { return passMark; }
    public List<ScoreBucket> getScoreDistribution() { return scoreDistribution; }
    public List<QuestionStat> getQuestionStats() { return questionStats; }
    public List<DifficultyStat> getDifficultyStats() { return difficultyStats; }
    public List<DepartmentStat> getDepartmentStats() { return departmentStats; }

    public static class ScoreBucket {
        private String label;
        private int count;
        public ScoreBucket(String label, int count) { this.label = label; this.count = count; }
        public String getLabel() { return label; }
        public int getCount() { return count; }
    }

    public static class QuestionStat {
        private Long questionId;
        private String text;
        private String type;
        private String difficulty;
        private long correct;
        private long wrong;
        private long pending;
        private long total;
        private Double successRate; // null when nothing graded yet

        public QuestionStat(Long questionId, String text, String type, String difficulty,
                            long correct, long wrong, long pending, long total, Double successRate) {
            this.questionId = questionId;
            this.text = text;
            this.type = type;
            this.difficulty = difficulty;
            this.correct = correct;
            this.wrong = wrong;
            this.pending = pending;
            this.total = total;
            this.successRate = successRate;
        }

        public Long getQuestionId() { return questionId; }
        public String getText() { return text; }
        public String getType() { return type; }
        public String getDifficulty() { return difficulty; }
        public long getCorrect() { return correct; }
        public long getWrong() { return wrong; }
        public long getPending() { return pending; }
        public long getTotal() { return total; }
        public Double getSuccessRate() { return successRate; }
    }

    public static class DifficultyStat {
        private String difficulty;
        private long questionCount;
        private long correct;
        private long wrong;
        private Double successRate;

        public DifficultyStat(String difficulty, long questionCount, long correct, long wrong, Double successRate) {
            this.difficulty = difficulty;
            this.questionCount = questionCount;
            this.correct = correct;
            this.wrong = wrong;
            this.successRate = successRate;
        }

        public String getDifficulty() { return difficulty; }
        public long getQuestionCount() { return questionCount; }
        public long getCorrect() { return correct; }
        public long getWrong() { return wrong; }
        public Double getSuccessRate() { return successRate; }
    }

    public static class DepartmentStat {
        private String departmentName;
        private long participants;
        private BigDecimal avgScore;

        public DepartmentStat(String departmentName, long participants, BigDecimal avgScore) {
            this.departmentName = departmentName;
            this.participants = participants;
            this.avgScore = avgScore;
        }

        public String getDepartmentName() { return departmentName; }
        public long getParticipants() { return participants; }
        public BigDecimal getAvgScore() { return avgScore; }
    }
}
