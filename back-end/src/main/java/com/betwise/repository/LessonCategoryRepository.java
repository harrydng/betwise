package com.betwise.repository;

import com.betwise.model.LessonCategory;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LessonCategoryRepository
        extends JpaRepository<LessonCategory, Long> {

    Optional<LessonCategory> findBySlugIgnoreCase(String slug);
}