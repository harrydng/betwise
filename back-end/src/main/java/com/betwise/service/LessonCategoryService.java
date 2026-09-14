package com.betwise.service;

import com.betwise.model.LessonCategory;
import com.betwise.repository.LessonCategoryRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LessonCategoryService {

    private final LessonCategoryRepository
            lessonCategoryRepository;

    public LessonCategoryService(
            LessonCategoryRepository
                    lessonCategoryRepository) {

        this.lessonCategoryRepository =
                lessonCategoryRepository;
    }

    public List<LessonCategory> getCategories() {

        return lessonCategoryRepository.findAll();
    }
}