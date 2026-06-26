package com.ces.exam.payload.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class EmployeeNotificationResponse {
    private int unreadCount;
    private List<Item> items;

    public EmployeeNotificationResponse(int unreadCount, List<Item> items) {
        this.unreadCount = unreadCount;
        this.items = items;
    }

    public int getUnreadCount() { return unreadCount; }
    public List<Item> getItems() { return items; }

    public static class Item {
        private Long examId;
        private Long sessionId;
        private Long assignmentId;
        private String examTitle;
        private String type;        // ASSIGNED | DEADLINE | RESULT
        private BigDecimal score;
        private Boolean passed;
        private LocalDateTime deadline;
        private LocalDateTime time;
        private boolean unread;

        public Item(Long examId, Long sessionId, Long assignmentId, String examTitle, String type,
                    BigDecimal score, Boolean passed, LocalDateTime deadline, LocalDateTime time, boolean unread) {
            this.examId = examId;
            this.sessionId = sessionId;
            this.assignmentId = assignmentId;
            this.examTitle = examTitle;
            this.type = type;
            this.score = score;
            this.passed = passed;
            this.deadline = deadline;
            this.time = time;
            this.unread = unread;
        }

        public Long getExamId() { return examId; }
        public Long getSessionId() { return sessionId; }
        public Long getAssignmentId() { return assignmentId; }
        public String getExamTitle() { return examTitle; }
        public String getType() { return type; }
        public BigDecimal getScore() { return score; }
        public Boolean getPassed() { return passed; }
        public LocalDateTime getDeadline() { return deadline; }
        public LocalDateTime getTime() { return time; }
        public boolean isUnread() { return unread; }
    }
}
