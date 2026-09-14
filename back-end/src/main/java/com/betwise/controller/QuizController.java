package com.betwise.controller;

import com.betwise.dto.*;
import com.betwise.model.User;
import com.betwise.service.QuizService;

import org.springframework.http.ResponseEntity;

import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quizzes")
public class QuizController {

    private final QuizService quizService;

    public QuizController(
            QuizService quizService) {

        this.quizService = quizService;
    }

    @GetMapping("/search")
    public ResponseEntity<List<QuizSummaryResponse>>
            searchQuizzes(
                    Authentication authentication,
                    @RequestParam String query) {

        User user =
                (User) authentication.getPrincipal();

        return ResponseEntity.ok(
                quizService.searchQuizzes(
                        user,
                        query));
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuizResponse>
            getQuiz(
                    Authentication authentication,
                    @PathVariable Long id) {

        User user =
                (User) authentication.getPrincipal();

        return ResponseEntity.ok(
                quizService.getQuiz(
                        user,
                        id));
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<QuizProgressResponse>
            startQuiz(
                    Authentication authentication,
                    @PathVariable Long id) {

        User user =
                (User) authentication.getPrincipal();

        return ResponseEntity.ok(
                quizService.startQuiz(
                        user,
                        id));
    }

    @PostMapping("/{id}/answers")
    public ResponseEntity<QuizAnswerResponse>
            submitAnswer(
                    Authentication authentication,
                    @PathVariable Long id,
                    @RequestBody
                    SubmitQuizAnswerRequest request) {

        User user =
                (User) authentication.getPrincipal();

        return ResponseEntity.ok(
                quizService.submitAnswer(
                        user,
                        id,
                        request));
    }

    @GetMapping("/{id}/progress")
    public ResponseEntity<QuizProgressResponse>
            getProgress(
                    Authentication authentication,
                    @PathVariable Long id) {

        User user =
                (User) authentication.getPrincipal();

        return ResponseEntity.ok(
                quizService.getProgress(
                        user,
                        id));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<QuizProgressResponse>
            completeQuiz(
                    Authentication authentication,
                    @PathVariable Long id,
                    @RequestBody
                    CompleteQuizRequest request) {

        User user =
                (User) authentication.getPrincipal();

        return ResponseEntity.ok(
                quizService.completeQuiz(
                        user,
                        id,
                        request));
    }
}