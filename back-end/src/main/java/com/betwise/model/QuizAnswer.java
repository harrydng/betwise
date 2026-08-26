package com.betwise.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "quiz_answers",
    uniqueConstraints = {
        @UniqueConstraint(
            columnNames = {"user_id", "quiz_question_id"}
        )
    }
)
public class QuizAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_question_id", nullable = false)
    private QuizQuestion quizQuestion;

    @Column(
        name = "selected_answer",
        nullable = false,
        length = 255
    )
    private String selectedAnswer;

    @Column(name = "is_correct", nullable = false)
    private Boolean correct;

    @Column(
        name = "cash_earned",
        nullable = false,
        precision = 15,
        scale = 2
    )
    private BigDecimal cashEarned;

    @Column(name = "answered_at", nullable = false)
    private LocalDateTime answeredAt;

    public QuizAnswer() {}

    @PrePersist
    protected void onCreate() {
        answeredAt = LocalDateTime.now();
    }
}