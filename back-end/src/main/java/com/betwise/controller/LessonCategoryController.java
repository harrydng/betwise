package com.betwise.controller;

import com.betwise.model.LessonCategory;
import com.betwise.service.LessonCategoryService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lesson-categories")
public class LessonCategoryController {

    private final LessonCategoryService
            lessonCategoryService;

    public LessonCategoryController(
            LessonCategoryService
                    lessonCategoryService) {

        this.lessonCategoryService =
                lessonCategoryService;
    }

    @GetMapping
    public ResponseEntity<List<LessonCategory>>
            getCategories() {

        return ResponseEntity.ok(
                lessonCategoryService
                        .getCategories());
    }
}