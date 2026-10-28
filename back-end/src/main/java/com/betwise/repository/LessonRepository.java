package com.betwise.repository;

import com.betwise.model.Lesson;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LessonRepository
        extends JpaRepository<Lesson, Long> {

    // MARK: - App (published and not archived)

    List<Lesson> findByPublishedTrueAndArchivedFalseOrderByPositionAscIdAsc();

    List<Lesson> findByCategorySlugIgnoreCaseAndPublishedTrueAndArchivedFalseOrderByPositionAscIdAsc(
            String slug);

    List<Lesson> findByTitleContainingIgnoreCaseAndPublishedTrueAndArchivedFalseOrderByPositionAscIdAsc(
            String title);

    // MARK: - Admin (everything, including drafts and archived lessons)

    List<Lesson> findAllByOrderByPositionAscIdAsc();

    long countByCategoryId(Long categoryId);
}
