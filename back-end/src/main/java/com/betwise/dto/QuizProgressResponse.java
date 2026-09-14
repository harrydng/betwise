package com.betwise.dto;

import com.betwise.model.QuizProgress;

public class QuizProgressResponse {

    private Long quizId;
    private int currentQuestion;
    private int score;
    private boolean completed;

    public QuizProgressResponse(
            QuizProgress progress) {

        this.quizId =
                progress.getQuiz().getId();

        this.currentQuestion =
                progress.getCurrentQuestion();

        this.score =
                progress.getScore();

        this.completed =
                progress.isCompleted();
    }

    public Long getQuizId() {
        return quizId;
    }

    public int getCurrentQuestion() {
        return currentQuestion;
    }

    public int getScore() {
        return score;
    }

    public boolean isCompleted() {
        return completed;
    }
}