package com.betwise.repository;

import com.betwise.model.QuizProgress;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuizProgressRepository
        extends JpaRepository<QuizProgress, Long> {

    Optional<QuizProgress> findByUserIdAndQuizId(
            Long userId,
            Long quizId);
}