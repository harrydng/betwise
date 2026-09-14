package com.betwise.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(
        name = "quiz_questions",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"quiz_id", "question_order"}
                )
        }
)
public class QuizQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String question;

    @Column(nullable = false, columnDefinition = "JSON")
    private String choices;

    @Column(
            name = "correct_answer",
            nullable = false,
            length = 255
    )
    private String correctAnswer;

    @Column(name = "question_order", nullable = false)
    private Integer questionOrder;

    @Column(
            name = "cash_reward",
            nullable = false,
            precision = 15,
            scale = 2
    )
    private BigDecimal cashReward;

    public QuizQuestion() {
    }

    public Long getId() {
        return id;
    }

    public Quiz getQuiz() {
        return quiz;
    }

    public String getQuestion() {
        return question;
    }

    public String getChoices() {
        return choices;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public Integer getQuestionOrder() {
        return questionOrder;
    }

    public BigDecimal getCashReward() {
        return cashReward;
    }

    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public void setChoices(String choices) {
        this.choices = choices;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public void setQuestionOrder(Integer questionOrder) {
        this.questionOrder = questionOrder;
    }

    public void setCashReward(BigDecimal cashReward) {
        this.cashReward = cashReward;
    }
}