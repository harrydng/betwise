package com.betwise.service;

import com.betwise.exception.ResourceNotFoundException;
import com.betwise.dto.*;
import com.betwise.model.Lesson;
import com.betwise.model.LessonProgress;
import com.betwise.model.User;

import com.betwise.repository.LessonCheckpointRepository;
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
    private final LessonCheckpointRepository checkpointRepository;
    private final PortfolioService portfolioService;
    private final NotificationService notificationService;

    public LessonService(
            LessonRepository lessonRepository,
            LessonProgressRepository lessonProgressRepository,
            LessonCheckpointRepository checkpointRepository,
            PortfolioService portfolioService,
            NotificationService notificationService) {

        this.lessonRepository = lessonRepository;
        this.lessonProgressRepository = lessonProgressRepository;
        this.checkpointRepository = checkpointRepository;
        this.portfolioService = portfolioService;
        this.notificationService = notificationService;
    }

    // MARK: - Browsing (only published, non-archived lessons)

    public List<LessonResponse> getLessons() {

        return lessonRepository
                .findByPublishedTrueAndArchivedFalseOrderByPositionAscIdAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<LessonResponse> getLessonsByCategory(
            String category) {

        return lessonRepository
                .findByCategorySlugIgnoreCaseAndPublishedTrueAndArchivedFalseOrderByPositionAscIdAsc(category)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public LessonResponse getLesson(Long lessonId) {

        return toResponse(getLiveLesson(lessonId));
    }

    public List<LessonResponse> searchLessons(
            String query) {

        if (query == null
                || query.trim().isEmpty()) {
            return List.of();
        }

        return lessonRepository
                .findByTitleContainingIgnoreCaseAndPublishedTrueAndArchivedFalseOrderByPositionAscIdAsc(
                        query.trim())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * The lesson's sections, in reading order. Still available for archived
     * lessons a user has already started, so they can finish them.
     */
    public List<LessonCheckpointResponse> getCheckpoints(
            User user,
            Long lessonId) {

        Lesson lesson = getLessonEntity(lessonId);

        boolean hasStarted = lessonProgressRepository
                .findByUserIdAndLessonId(user.getId(), lessonId)
                .isPresent();

        if (!lesson.isLive() && !hasStarted) {
            throw new ResourceNotFoundException("Lesson not found");
        }

        return checkpointRepository
                .findByLessonIdOrderByPositionAsc(lessonId)
                .stream()
                .map(LessonCheckpointResponse::new)
                .toList();
    }

    @Transactional
    public LessonProgressResponse startLesson(
            User user,
            Long lessonId) {

        Lesson lesson = getLiveLesson(lessonId);

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

            long checkpointCount = checkpointRepository.countByLessonId(lessonId);

            if (checkpointCount > 0
                    && request.getCompletedCheckpoints() > checkpointCount) {
                throw new IllegalArgumentException(
                        "This lesson only has " + checkpointCount + " checkpoints");
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

        notificationService.notifyLessonReward(
                user,
                lesson.getId(),
                lesson.getTitle(),
                lesson.getCashReward());

        return new LessonProgressResponse(saved);
    }

    private Lesson getLessonEntity(
            Long lessonId) {

        return lessonRepository
                .findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Lesson not found"));
    }

    /** Drafts and archived lessons look like they don't exist to app users. */
    private Lesson getLiveLesson(
            Long lessonId) {

        Lesson lesson = getLessonEntity(lessonId);

        if (!lesson.isLive()) {
            throw new ResourceNotFoundException("Lesson not found");
        }

        return lesson;
    }

    private LessonResponse toResponse(Lesson lesson) {

        return new LessonResponse(
                lesson,
                checkpointRepository.countByLessonId(lesson.getId()));
    }
}