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

    // Drafts are only visible in the admin app. Lessons that existed before
    // this column was added default to published so they stay visible.
    @Column(nullable = false, columnDefinition = "boolean not null default true")
    private boolean published = false;

    // Archived lessons are hidden from the app but kept, because users'
    // progress and rewards still reference them.
    @Column(nullable = false, columnDefinition = "boolean not null default false")
    private boolean archived = false;

    // Display order within the app, lowest first.
    @Column(nullable = false, columnDefinition = "int not null default 0")
    private int position = 0;

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

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    /** Visible to app users: published and not archived. */
    public boolean isLive() {
        return published && !archived;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
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