package com.betwise.service;

import com.betwise.dto.*;
import com.betwise.model.Lesson;
import com.betwise.model.LessonProgress;
import com.betwise.model.User;

import com.betwise.repository.LessonProgressRepository;
import com.betwise.repository.LessonRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LessonService {

    private final LessonRepository lessonRepository;
    private final LessonProgressRepository lessonProgressRepository;
    private final PortfolioService portfolioService;

    public LessonService(
            LessonRepository lessonRepository,
            LessonProgressRepository lessonProgressRepository,
            PortfolioService portfolioService) {

        this.lessonRepository = lessonRepository;
        this.lessonProgressRepository = lessonProgressRepository;
        this.portfolioService = portfolioService;
    }

    public List<LessonResponse> getLessons() {

        return lessonRepository
                .findAll()
                .stream()
                .map(LessonResponse::new)
                .toList();
    }

    public List<LessonResponse> getLessonsByCategory(
            String category) {

        return lessonRepository
                .findByCategorySlugIgnoreCase(category)
                .stream()
                .map(LessonResponse::new)
                .toList();
    }

    public LessonResponse getLesson(Long lessonId) {

        return new LessonResponse(
                getLessonEntity(lessonId));
    }

    public List<LessonResponse> searchLessons(
            String query) {

        if (query == null
                || query.trim().isEmpty()) {
            return List.of();
        }

        return lessonRepository
                .findByTitleContainingIgnoreCase(
                        query.trim())
                .stream()
                .map(LessonResponse::new)
                .toList();
    }

    @Transactional
    public LessonProgressResponse startLesson(
            User user,
            Long lessonId) {

        Lesson lesson = getLessonEntity(lessonId);

        LessonProgress progress = lessonProgressRepository
                .findByUserIdAndLessonId(
                        user.getId(),
                        lessonId)
                .orElse(null);

        if (progress == null) {

            progress = new LessonProgress(
                    user,
                    lesson);
        }

        progress.setLastAccessedAt(
                LocalDateTime.now());

        LessonProgress saved = lessonProgressRepository
                .save(progress);

        return new LessonProgressResponse(saved);
    }

    public LessonProgressResponse getProgress(
            User user,
            Long lessonId) {

        getLessonEntity(lessonId);

        LessonProgress progress = lessonProgressRepository
                .findByUserIdAndLessonId(
                        user.getId(),
                        lessonId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Lesson has not been started"));

        return new LessonProgressResponse(progress);
    }

    @Transactional
    public LessonProgressResponse updateProgress(
            User user,
            Long lessonId,
            UpdateLessonProgressRequest request) {

        LessonProgress progress = lessonProgressRepository
                .findByUserIdAndLessonId(
                        user.getId(),
                        lessonId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Lesson has not been started"));

        if (request.getCurrentCheckpoint() != null) {

            if (request.getCurrentCheckpoint() < 0) {
                throw new IllegalArgumentException(
                        "Checkpoint cannot be negative");
            }

            progress.setCurrentCheckpoint(
                    request.getCurrentCheckpoint());
        }

        if (request.getCompletedCheckpoints() != null) {

            if (request.getCompletedCheckpoints() < 0) {
                throw new IllegalArgumentException(
                        "Completed checkpoints cannot be negative");
            }

            progress.setCompletedCheckpoints(
                    request.getCompletedCheckpoints());
        }

        progress.setLastAccessedAt(
                LocalDateTime.now());

        return new LessonProgressResponse(
                lessonProgressRepository
                        .save(progress));
    }

    @Transactional
    public LessonProgressResponse completeLesson(
            User user,
            Long lessonId,
            Long portfolioId) {

        Lesson lesson = getLessonEntity(lessonId);

        LessonProgress progress = lessonProgressRepository
                .findByUserIdAndLessonId(
                        user.getId(),
                        lessonId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Lesson has not been started"));

        if (progress.isCompleted()) {
            return new LessonProgressResponse(progress);
        }

        progress.setCompleted(true);
        progress.setCompletedAt(LocalDateTime.now());

        LessonProgress saved = lessonProgressRepository.save(progress);

        portfolioService.addLessonReward(
                user,
                portfolioId,
                lesson.getCashReward());

        return new LessonProgressResponse(saved);
    }

    private Lesson getLessonEntity(
            Long lessonId) {

        return lessonRepository
                .findById(lessonId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Lesson not found"));
    }
}