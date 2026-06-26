package com.ces.exam.payload.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class ProgressResponse {
    private int completed;
    private Integer avgScore;
    private Integer bestScore;
    private int passed;
    private Integer departmentAvg;
    private String departmentName;
    private List<TrendPoint> trend;
    private List<CategoryStat> categories;

    public ProgressResponse(int completed, Integer avgScore, Integer bestScore, int passed,
                            Integer departmentAvg, String departmentName,
                            List<TrendPoint> trend, List<CategoryStat> categories) {
        this.completed = completed;
        this.avgScore = avgScore;
        this.bestScore = bestScore;
        this.passed = passed;
        this.departmentAvg = departmentAvg;
        this.departmentName = departmentName;
        this.trend = trend;
        this.categories = categories;
    }

    public int getCompleted() { return completed; }
    public Integer getAvgScore() { return avgScore; }
    public Integer getBestScore() { return bestScore; }
    public int getPassed() { return passed; }
    public Integer getDepartmentAvg() { return departmentAvg; }
    public String getDepartmentName() { return departmentName; }
    public List<TrendPoint> getTrend() { return trend; }
    public List<CategoryStat> getCategories() { return categories; }

    public static class TrendPoint {
        private String examTitle;
        private BigDecimal score;
        private Boolean passed;
        private LocalDateTime date;

        public TrendPoint(String examTitle, BigDecimal score, Boolean passed, LocalDateTime date) {
            this.examTitle = examTitle;
            this.score = score;
            this.passed = passed;
            this.date = date;
        }

        public String getExamTitle() { return examTitle; }
        public BigDecimal getScore() { return score; }
        public Boolean getPassed() { return passed; }
        public LocalDateTime getDate() { return date; }
    }

    public static class CategoryStat {
        private String name;
        private long correct;
        private long graded;
        private Integer successRate;

        public CategoryStat(String name, long correct, long graded, Integer successRate) {
            this.name = name;
            this.correct = correct;
            this.graded = graded;
            this.successRate = successRate;
        }

        public String getName() { return name; }
        public long getCorrect() { return correct; }
        public long getGraded() { return graded; }
        public Integer getSuccessRate() { return successRate; }
    }
}
