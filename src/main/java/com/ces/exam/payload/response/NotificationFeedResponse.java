package com.ces.exam.payload.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class NotificationFeedResponse {
    private int unreadCount;
    private List<Item> items;

    public NotificationFeedResponse(int unreadCount, List<Item> items) {
        this.unreadCount = unreadCount;
        this.items = items;
    }

    public int getUnreadCount() { return unreadCount; }
    public List<Item> getItems() { return items; }

    public static class Item {
        private Long sessionId;
        private Long examId;
        private String examTitle;
        private String userName;
        private BigDecimal score;
        private Boolean passed;
        private int pendingGrading;
        private int violations;
        private String type;        // GRADING | VIOLATION | RESULT
        private LocalDateTime time;
        private boolean unread;

        public Item(Long sessionId, Long examId, String examTitle, String userName, BigDecimal score,
                    Boolean passed, int pendingGrading, int violations, String type,
                    LocalDateTime time, boolean unread) {
            this.sessionId = sessionId;
            this.examId = examId;
            this.examTitle = examTitle;
            this.userName = userName;
            this.score = score;
            this.passed = passed;
            this.pendingGrading = pendingGrading;
            this.violations = violations;
            this.type = type;
            this.time = time;
            this.unread = unread;
        }

        public Long getSessionId() { return sessionId; }
        public Long getExamId() { return examId; }
        public String getExamTitle() { return examTitle; }
        public String getUserName() { return userName; }
        public BigDecimal getScore() { return score; }
        public Boolean getPassed() { return passed; }
        public int getPendingGrading() { return pendingGrading; }
        public int getViolations() { return violations; }
        public String getType() { return type; }
        public LocalDateTime getTime() { return time; }
        public boolean isUnread() { return unread; }
    }
}
