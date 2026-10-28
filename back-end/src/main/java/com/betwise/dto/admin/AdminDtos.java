package com.betwise.dto.admin;

import com.betwise.model.Lesson;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Request and response shapes for /api/admin. Kept together because they
 * only make sense as a set and are used by the admin web app alone.
 */
public final class AdminDtos {

    private AdminDtos() {
    }

    // MARK: - Categories

    /** Leave `slug` blank to derive it from the name. */
    public record CategoryRequest(
            String name,
            String slug,
            String description
    ) {
    }

    public record CategoryResponse(
            Long id,
            String name,
            String slug,
            String description,
            long lessonCount
    ) {
    }

    // MARK: - Lessons

    /**
     * Used for both create and update. On update, fields left null keep
     * their current value.
     */
    public record LessonRequest(
            String title,
            String description,
            Long categoryId,
            Lesson.Topic topic,
            Lesson.Difficulty difficulty,
            BigDecimal cashReward,
            Integer position,
            Boolean published,
            Boolean archived
    ) {
    }

    public record LessonSummary(
            Long id,
            String title,
            Long categoryId,
            String category,
            Lesson.Topic topic,
            Lesson.Difficulty difficulty,
            BigDecimal cashReward,
            int position,
            boolean published,
            boolean archived,
            long checkpointCount,
            long quizCount,
            long learnerCount,
            LocalDateTime updatedAt
    ) {
    }

    public record LessonDetail(
            LessonSummary summary,
            String description,
            List<CheckpointResponse> checkpoints,
            List<QuizResponse> quizzes
    ) {
    }

    // MARK: - Checkpoints

    public record CheckpointInput(
            String title,
            String body
    ) {
    }

    public record CheckpointResponse(
            Long id,
            int position,
            String title,
            String body
    ) {
    }

    // MARK: - Quizzes

    /** Questions are saved in the order given. */
    public record QuizRequest(
            String title,
            Integer checkpointOrder,
            List<QuestionInput> questions
    ) {
    }

    /** Include `id` to update an existing question; omit it to add one. */
    public record QuestionInput(
            Long id,
            String question,
            List<String> choices,
            String correctAnswer
    ) {
    }

    public record QuizResponse(
            Long id,
            String title,
            int checkpointOrder,
            boolean hasAttempts,
            List<QuestionResponse> questions
    ) {
    }

    public record QuestionResponse(
            Long id,
            int questionOrder,
            String question,
            List<String> choices,
            String correctAnswer
    ) {
    }
}
