package com.betwise.repository;

import com.betwise.model.QuizAnswer;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuizAnswerRepository
        extends JpaRepository<QuizAnswer, Long> {

    List<QuizAnswer> findByUserIdAndQuizQuestionQuizId(
            Long userId,
            Long quizId);

    Optional<QuizAnswer> findByUserIdAndQuizQuestionId(
            Long userId,
            Long quizQuestionId);

    boolean existsByUserIdAndQuizQuestionId(
            Long userId,
            Long quizQuestionId);
}