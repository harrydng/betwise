package com.betwise.dto;

import com.betwise.model.Lesson;

import java.math.BigDecimal;

public class LessonResponse {

    private Long id;

    private String title;
    private String description;

    private String category;
    private String categorySlug;

    private Lesson.Topic topic;
    private Lesson.Difficulty difficulty;

    private BigDecimal cashReward;

    public LessonResponse(Lesson lesson) {

        this.id = lesson.getId();

        this.title = lesson.getTitle();
        this.description = lesson.getDescription();

        this.category =
                lesson.getCategory().getName();

        this.categorySlug =
                lesson.getCategory().getSlug();

        this.topic = lesson.getTopic();
        this.difficulty = lesson.getDifficulty();

        this.cashReward =
                lesson.getCashReward();
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

    public String getCategory() {
        return category;
    }

    public String getCategorySlug() {
        return categorySlug;
    }

    public Lesson.Topic getTopic() {
        return topic;
    }

    public Lesson.Difficulty getDifficulty() {
        return difficulty;
    }

    public BigDecimal getCashReward() {
        return cashReward;
    }
}