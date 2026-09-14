package com.betwise.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "quiz_progress",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"user_id", "quiz_id"}
                )
        }
)
public class QuizProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @Column(name = "current_question", nullable = false)
    private Integer currentQuestion = 1;

    @Column(nullable = false)
    private Integer score = 0;

    @Column(nullable = false)
    private Boolean completed = false;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    public QuizProgress() {
    }

    public QuizProgress(
            User user,
            Quiz quiz) {

        this.user = user;
        this.quiz = quiz;
    }

    @PrePersist
    protected void onCreate() {
        startedAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Quiz getQuiz() {
        return quiz;
    }

    public Integer getCurrentQuestion() {
        return currentQuestion;
    }

    public Integer getScore() {
        return score;
    }

    public boolean isCompleted() {
        return Boolean.TRUE.equals(completed);
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCurrentQuestion(Integer currentQuestion) {
        this.currentQuestion = currentQuestion;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}