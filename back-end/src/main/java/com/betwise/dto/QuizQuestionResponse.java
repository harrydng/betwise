package com.betwise.dto;

import com.betwise.model.QuizQuestion;

public class QuizQuestionResponse {

    private Long id;
    private String question;
    private String choices;
    private int questionOrder;

    public QuizQuestionResponse(
            QuizQuestion question) {

        this.id = question.getId();
        this.question = question.getQuestion();
        this.choices = question.getChoices();
        this.questionOrder =
                question.getQuestionOrder();
    }

    public Long getId() {
        return id;
    }

    public String getQuestion() {
        return question;
    }

    public String getChoices() {
        return choices;
    }

    public int getQuestionOrder() {
        return questionOrder;
    }
}