package com.betwise.repository;

import com.betwise.model.Quiz;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizRepository
        extends JpaRepository<Quiz, Long> {

    List<Quiz> findByTitleContainingIgnoreCase(
            String title);

    List<Quiz> findByLessonIdOrderByCheckpointOrderAsc(
            Long lessonId);
}