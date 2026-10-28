package com.betwise.dto;

import com.betwise.model.Notification;

import java.time.LocalDateTime;

public class NotificationResponse {

    private Long id;
    private Notification.NotificationType type;
    private String title;
    private String body;
    private Long relatedId;
    private boolean read;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;

    public NotificationResponse(Notification notification) {
        this.id = notification.getId();
        this.type = notification.getType();
        this.title = notification.getTitle();
        this.body = notification.getBody();
        this.relatedId = notification.getRelatedId();
        this.read = notification.isRead();
        this.readAt = notification.getReadAt();
        this.createdAt = notification.getCreatedAt();
    }

    public Long getId() {
        return id;
    }

    public Notification.NotificationType getType() {
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

    public boolean isRead() {
        return read;
    }

    public LocalDateTime getReadAt() {
        return readAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
