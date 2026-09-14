package com.betwise.dto;

import com.betwise.model.LessonProgress;

import java.time.LocalDateTime;

public class LessonProgressResponse {

    private Long lessonId;
    private boolean completed;
    private Integer currentCheckpoint;
    private Integer completedCheckpoints;

    private LocalDateTime startedAt;
    private LocalDateTime lastAccessedAt;
    private LocalDateTime completedAt;

    public LessonProgressResponse(
            LessonProgress progress) {

        this.lessonId =
                progress.getLesson().getId();

        this.completed =
                progress.isCompleted();

        this.currentCheckpoint =
                progress.getCurrentCheckpoint();

        this.completedCheckpoints =
                progress.getCompletedCheckpoints();

        this.startedAt =
                progress.getStartedAt();

        this.lastAccessedAt =
                progress.getLastAccessedAt();

        this.completedAt =
                progress.getCompletedAt();
    }

    public Long getLessonId() {
        return lessonId;
    }

    public boolean isCompleted() {
        return completed;
    }

    public Integer getCurrentCheckpoint() {
        return currentCheckpoint;
    }

    public Integer getCompletedCheckpoints() {
        return completedCheckpoints;
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
}