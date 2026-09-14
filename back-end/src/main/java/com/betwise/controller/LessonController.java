package com.betwise.controller;

import com.betwise.dto.*;
import com.betwise.model.User;
import com.betwise.service.LessonService;
import com.betwise.service.QuizService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lessons")
public class LessonController {

    private final LessonService lessonService;
    private final QuizService quizService;

    public LessonController(
            LessonService lessonService,
            QuizService quizService) {

        this.lessonService = lessonService;
        this.quizService = quizService;
    }

    @GetMapping
    public ResponseEntity<List<LessonResponse>> getLessons(
            @RequestParam(required = false) String category) {

        if (category != null
                && !category.isBlank()) {

            return ResponseEntity.ok(
                    lessonService
                            .getLessonsByCategory(
                                    category));
        }

        return ResponseEntity.ok(
                lessonService.getLessons());
    }

    @GetMapping("/search")
    public ResponseEntity<List<LessonResponse>> searchLessons(
            @RequestParam String query) {

        return ResponseEntity.ok(
                lessonService
                        .searchLessons(query));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LessonResponse> getLesson(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                lessonService.getLesson(id));
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<LessonProgressResponse> startLesson(
            Authentication authentication,
            @PathVariable Long id) {

        User user = (User) authentication.getPrincipal();

        return ResponseEntity.ok(
                lessonService.startLesson(
                        user,
                        id));
    }

    @GetMapping("/{id}/progress")
    public ResponseEntity<LessonProgressResponse> getProgress(
            Authentication authentication,
            @PathVariable Long id) {

        User user = (User) authentication.getPrincipal();

        return ResponseEntity.ok(
                lessonService.getProgress(
                        user,
                        id));
    }

    @PatchMapping("/{id}/progress")
    public ResponseEntity<LessonProgressResponse> updateProgress(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody UpdateLessonProgressRequest request) {

        User user = (User) authentication.getPrincipal();

        return ResponseEntity.ok(
                lessonService.updateProgress(
                        user,
                        id,
                        request));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<LessonProgressResponse> completeLesson(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody CompleteLessonRequest request) {

        User user = (User) authentication.getPrincipal();

        return ResponseEntity.ok(
                lessonService.completeLesson(
                        user,
                        id,
                        request.getPortfolioId()));
    }

    @GetMapping("/{id}/quizzes")
    public ResponseEntity<List<QuizResponse>> getLessonQuizzes(
            Authentication authentication,
            @PathVariable Long id) {

        User user = (User) authentication.getPrincipal();

        return ResponseEntity.ok(
                quizService.getQuizzesByLesson(
                        user,
                        id));
    }
}