package com.betwise.repository;

import com.betwise.model.LessonProgress;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LessonProgressRepository
        extends JpaRepository<LessonProgress, Long> {

    Optional<LessonProgress> findByUserIdAndLessonId(
            Long userId,
            Long lessonId);
}