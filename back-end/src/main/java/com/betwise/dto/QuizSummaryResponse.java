package com.betwise.dto;

public class QuizSummaryResponse {

    private Long id;
    private String title;

    private Long lessonId;
    private String lessonTitle;

    private boolean locked;

    public QuizSummaryResponse(
            Long id,
            String title,
            Long lessonId,
            String lessonTitle,
            boolean locked) {

        this.id = id;
        this.title = title;
        this.lessonId = lessonId;
        this.lessonTitle = lessonTitle;
        this.locked = locked;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public Long getLessonId() {
        return lessonId;
    }

    public String getLessonTitle() {
        return lessonTitle;
    }

    public boolean isLocked() {
        return locked;
    }
}