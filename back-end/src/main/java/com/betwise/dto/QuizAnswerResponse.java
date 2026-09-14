package com.betwise.dto;

public class QuizAnswerResponse {

    private Long questionId;
    private boolean correct;

    public QuizAnswerResponse(
            Long questionId,
            boolean correct) {

        this.questionId = questionId;
        this.correct = correct;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public boolean isCorrect() {
        return correct;
    }
}