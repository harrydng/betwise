package com.betwise.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "notifications",
        indexes = @Index(
                name = "idx_notifications_user_created",
                columnList = "user_id, created_at"
        )
)
public class Notification {

    public enum NotificationType {
        TRADE_FILLED,
        CASH_TRANSFER,
        LESSON_REWARD,
        QUIZ_COMPLETED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private NotificationType type;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(length = 500)
    private String body;

    // The id of whatever the notification is about (a transaction, a
    // lesson, a quiz...). Its meaning depends on `type`.
    @Column(name = "related_id")
    private Long relatedId;

    // Null until the user has read the notification.
    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Notification() {
    }

    public Notification(
            User user,
            NotificationType type,
            String title,
            String body,
            Long relatedId) {
        this.user = user;
        this.type = type;
        this.title = title;
        this.body = body;
        this.relatedId = relatedId;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public NotificationType getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public Long getRelatedId() {
        return relatedId;
    }

    public LocalDateTime getReadAt() {
        return readAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isRead() {
        return readAt != null;
    }

    public void setReadAt(LocalDateTime readAt) {
        this.readAt = readAt;
    }
}
