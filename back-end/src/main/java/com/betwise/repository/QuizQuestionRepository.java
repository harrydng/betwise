package com.betwise.repository;

import com.betwise.model.QuizQuestion;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizQuestionRepository
        extends JpaRepository<QuizQuestion, Long> {

    List<QuizQuestion> findByQuizIdOrderByQuestionOrderAsc(
            Long quizId);
}