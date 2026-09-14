package com.betwise.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "lessons")
public class Lesson {

    public enum Topic {
        STOCKS,
        BONDS,
        ETFS,
        DIVERSIFICATION,
        RISK,
        SHORT_SELLING,
        FUNDAMENTAL_ANALYSIS,
        TECHNICAL_ANALYSIS,
        REAL_ESTATE
    }

    public enum Difficulty {
        BEGINNER,
        INTERMEDIATE,
        ADVANCED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private LessonCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Topic topic;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Difficulty difficulty;

    @Column(name = "cash_reward", nullable = false, precision = 15, scale = 2)
    private BigDecimal cashReward;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Lesson() {
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

    public LessonCategory getCategory() {
        return category;
    }

    public void setCategory(LessonCategory category) {
        this.category = category;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public Topic getTopic() {
        return topic;
    }

    public BigDecimal getCashReward() {
        return cashReward;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public void setCashReward(BigDecimal cashReward) {
        this.cashReward = cashReward;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

}