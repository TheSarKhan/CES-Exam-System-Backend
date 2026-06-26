package com.ces.exam.payload.response;

import java.util.List;

public class AnalyticsInsightsResponse {
    private List<QuestionInsight> hardestQuestions;
    private List<QuestionInsight> mostMissed;
    private List<ViolationStat> violationStats;
    private long totalViolations;
    private long flaggedSessions;

    public AnalyticsInsightsResponse(List<QuestionInsight> hardestQuestions, List<QuestionInsight> mostMissed,
                                     List<ViolationStat> violationStats, long totalViolations, long flaggedSessions) {
        this.hardestQuestions = hardestQuestions;
        this.mostMissed = mostMissed;
        this.violationStats = violationStats;
        this.totalViolations = totalViolations;
        this.flaggedSessions = flaggedSessions;
    }

    public List<QuestionInsight> getHardestQuestions() { return hardestQuestions; }
    public List<QuestionInsight> getMostMissed() { return mostMissed; }
    public List<ViolationStat> getViolationStats() { return violationStats; }
    public long getTotalViolations() { return totalViolations; }
    public long getFlaggedSessions() { return flaggedSessions; }

    public static class QuestionInsight {
        private Long questionId;
        private String text;
        private String type;
        private String difficulty;
        private long correct;
        private long wrong;
        private long total;
        private Double successRate;

        public QuestionInsight(Long questionId, String text, String type, String difficulty,
                               long correct, long wrong, long total, Double successRate) {
            this.questionId = questionId;
            this.text = text;
            this.type = type;
            this.difficulty = difficulty;
            this.correct = correct;
            this.wrong = wrong;
            this.total = total;
            this.successRate = successRate;
        }

        public Long getQuestionId() { return questionId; }
        public String getText() { return text; }
        public String getType() { return type; }
        public String getDifficulty() { return difficulty; }
        public long getCorrect() { return correct; }
        public long getWrong() { return wrong; }
        public long getTotal() { return total; }
        public Double getSuccessRate() { return successRate; }
    }

    public static class ViolationStat {
        private String type;
        private String label;
        private long count;

        public ViolationStat(String type, String label, long count) {
            this.type = type;
            this.label = label;
            this.count = count;
        }

        public String getType() { return type; }
        public String getLabel() { return label; }
        public long getCount() { return count; }
    }
}
