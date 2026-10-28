package com.betwise.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * One section of a lesson. Users read checkpoints in `position` order,
 * and finishing one advances their LessonProgress.completedCheckpoints.
 */
@Entity
@Table(
        name = "lesson_checkpoints",
        indexes = @Index(name = "idx_lesson_checkpoints_lesson", columnList = "lesson_id, position")
)
public class LessonCheckpoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    // 1-based, matching LessonProgress.currentCheckpoint.
    @Column(nullable = false)
    private int position;

    @Column(nullable = false, length = 150)
    private String title;

    // Markdown. The app renders inline styles (**bold**, *italic*, links)
    // and keeps line breaks, so "- " lines read as bullet points.
    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public LessonCheckpoint() {
    }

    public LessonCheckpoint(
            Lesson lesson,
            int position,
            String title,
            String body) {
        this.lesson = lesson;
        this.position = position;
        this.title = title;
        this.body = body;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Lesson getLesson() {
        return lesson;
    }

    public int getPosition() {
        return position;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
