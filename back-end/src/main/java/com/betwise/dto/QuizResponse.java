package com.betwise.dto;

import com.betwise.model.Quiz;

import java.util.List;

public class QuizResponse {

    private Long id;
    private String title;

    private Long lessonId;
    private String lessonTitle;

    private boolean locked;

    private List<QuizQuestionResponse> questions;

    public QuizResponse(
            Quiz quiz,
            boolean locked,
            List<QuizQuestionResponse> questions) {

        this.id = quiz.getId();
        this.title = quiz.getTitle();

        this.lessonId =
                quiz.getLesson().getId();

        this.lessonTitle =
                quiz.getLesson().getTitle();

        this.locked = locked;

        this.questions = questions;
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

    public List<QuizQuestionResponse> getQuestions() {
        return questions;
    }
}