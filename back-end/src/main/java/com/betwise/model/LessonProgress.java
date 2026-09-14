package com.betwise.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "lesson_progress",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"user_id", "lesson_id"}
                )
        }
)
public class LessonProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @Column(name = "current_checkpoint", nullable = false)
    private Integer currentCheckpoint = 1;

    @Column(name = "completed_checkpoints", nullable = false)
    private Integer completedCheckpoints = 0;

    @Column(nullable = false)
    private Boolean completed = false;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "last_accessed_at", nullable = false)
    private LocalDateTime lastAccessedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    public LessonProgress() {
    }

    public LessonProgress(
            User user,
            Lesson lesson) {

        this.user = user;
        this.lesson = lesson;
    }

    @PrePersist
    protected void onCreate() {
        startedAt = LocalDateTime.now();
        lastAccessedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        lastAccessedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Lesson getLesson() {
        return lesson;
    }

    public Integer getCurrentCheckpoint() {
        return currentCheckpoint;
    }

    public Integer getCompletedCheckpoints() {
        return completedCheckpoints;
    }

    public boolean isCompleted() {
        return Boolean.TRUE.equals(completed);
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public LocalDateTime getLastAccessedAt() {
        return lastAccessedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCurrentCheckpoint(
            Integer currentCheckpoint) {

        this.currentCheckpoint = currentCheckpoint;
    }

    public void setCompletedCheckpoints(
            Integer completedCheckpoints) {

        this.completedCheckpoints = completedCheckpoints;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }

    public void setCompletedAt(
            LocalDateTime completedAt) {

        this.completedAt = completedAt;
    }

    public void setLastAccessedAt(
            LocalDateTime lastAccessedAt) {

        this.lastAccessedAt = lastAccessedAt;
    }
}