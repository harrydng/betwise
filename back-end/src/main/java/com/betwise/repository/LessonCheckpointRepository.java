package com.betwise.repository;

import com.betwise.model.LessonCheckpoint;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LessonCheckpointRepository
        extends JpaRepository<LessonCheckpoint, Long> {

    List<LessonCheckpoint> findByLessonIdOrderByPositionAsc(Long lessonId);

    long countByLessonId(Long lessonId);

    void deleteByLessonId(Long lessonId);
}
