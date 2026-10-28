package com.betwise.controller;

import com.betwise.dto.admin.AdminDtos.*;
import com.betwise.service.AdminContentService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Content management for the admin web app. SecurityConfig restricts
 * everything under /api/admin to users with the ADMIN role.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminContentService contentService;

    public AdminController(
            AdminContentService contentService) {

        this.contentService = contentService;
    }

    // MARK: - Categories

    @GetMapping("/categories")
    public ResponseEntity<List<CategoryResponse>> getCategories() {
        return ResponseEntity.ok(contentService.getCategories());
    }

    @PostMapping("/categories")
    public ResponseEntity<CategoryResponse> createCategory(
            @RequestBody CategoryRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(contentService.createCategory(request));
    }

    @PatchMapping("/categories/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable Long id,
            @RequestBody CategoryRequest request) {

        return ResponseEntity.ok(contentService.updateCategory(id, request));
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Void> deleteCategory(
            @PathVariable Long id) {

        contentService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    // MARK: - Lessons

    @GetMapping("/lessons")
    public ResponseEntity<List<LessonSummary>> getLessons() {
        return ResponseEntity.ok(contentService.getLessons());
    }

    @GetMapping("/lessons/{id}")
    public ResponseEntity<LessonDetail> getLesson(
            @PathVariable Long id) {

        return ResponseEntity.ok(contentService.getLesson(id));
    }

    @PostMapping("/lessons")
    public ResponseEntity<LessonDetail> createLesson(
            @RequestBody LessonRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(contentService.createLesson(request));
    }

    @PatchMapping("/lessons/{id}")
    public ResponseEntity<LessonDetail> updateLesson(
            @PathVariable Long id,
            @RequestBody LessonRequest request) {

        return ResponseEntity.ok(contentService.updateLesson(id, request));
    }

    @DeleteMapping("/lessons/{id}")
    public ResponseEntity<Void> deleteLesson(
            @PathVariable Long id) {

        contentService.deleteLesson(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/lessons/{id}/checkpoints")
    public ResponseEntity<List<CheckpointResponse>> replaceCheckpoints(
            @PathVariable Long id,
            @RequestBody List<CheckpointInput> checkpoints) {

        return ResponseEntity.ok(contentService.replaceCheckpoints(id, checkpoints));
    }

    // MARK: - Quizzes

    @PostMapping("/lessons/{id}/quizzes")
    public ResponseEntity<QuizResponse> createQuiz(
            @PathVariable Long id,
            @RequestBody QuizRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(contentService.createQuiz(id, request));
    }

    @PutMapping("/quizzes/{id}")
    public ResponseEntity<QuizResponse> updateQuiz(
            @PathVariable Long id,
            @RequestBody QuizRequest request) {

        return ResponseEntity.ok(contentService.updateQuiz(id, request));
    }

    @DeleteMapping("/quizzes/{id}")
    public ResponseEntity<Void> deleteQuiz(
            @PathVariable Long id) {

        contentService.deleteQuiz(id);
        return ResponseEntity.noContent().build();
    }
}
