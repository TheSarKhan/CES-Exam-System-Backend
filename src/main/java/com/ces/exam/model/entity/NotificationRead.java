package com.ces.exam.model.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** Tracks, per admin, the moment they last cleared their notifications. */
@Entity
@Table(name = "notification_reads")
public class NotificationRead {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "last_read_at")
    private LocalDateTime lastReadAt;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public LocalDateTime getLastReadAt() { return lastReadAt; }
    public void setLastReadAt(LocalDateTime lastReadAt) { this.lastReadAt = lastReadAt; }
}
