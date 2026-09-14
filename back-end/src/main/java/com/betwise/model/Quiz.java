package com.betwise.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "quizzes",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"lesson_id", "checkpoint_order"}
                )
        }
)
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(name = "checkpoint_order", nullable = false)
    private Integer checkpointOrder;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Quiz() {
    }

    public Quiz(
            Lesson lesson,
            String title,
            Integer checkpointOrder) {

        this.lesson = lesson;
        this.title = title;
        this.checkpointOrder = checkpointOrder;
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

    public String getTitle() {
        return title;
    }

    public Integer getCheckpointOrder() {
        return checkpointOrder;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setLesson(Lesson lesson) {
        this.lesson = lesson;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setCheckpointOrder(Integer checkpointOrder) {
        this.checkpointOrder = checkpointOrder;
    }
}