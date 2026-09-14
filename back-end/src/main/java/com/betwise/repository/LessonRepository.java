package com.betwise.repository;

import com.betwise.model.Lesson;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LessonRepository
        extends JpaRepository<Lesson, Long> {

    List<Lesson> findByTitleContainingIgnoreCase(
            String title);

    List<Lesson> findByCategorySlugIgnoreCase(
            String slug);

    List<Lesson>
            findByCategorySlugIgnoreCaseAndTitleContainingIgnoreCase(
                    String slug,
                    String title);
}