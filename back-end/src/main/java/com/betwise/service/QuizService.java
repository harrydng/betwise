package com.betwise.service;

import com.betwise.dto.*;
import com.betwise.model.*;

import com.betwise.repository.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class QuizService {

    private final QuizRepository quizRepository;

    private final QuizQuestionRepository quizQuestionRepository;

    private final QuizProgressRepository quizProgressRepository;

    private final QuizAnswerRepository quizAnswerRepository;

    private final LessonProgressRepository lessonProgressRepository;

    public QuizService(
            QuizRepository quizRepository,
            QuizQuestionRepository quizQuestionRepository,
            QuizProgressRepository quizProgressRepository,
            QuizAnswerRepository quizAnswerRepository,
            LessonProgressRepository lessonProgressRepository) {

        this.quizRepository = quizRepository;
        this.quizQuestionRepository = quizQuestionRepository;

        this.quizProgressRepository = quizProgressRepository;

        this.quizAnswerRepository = quizAnswerRepository;

        this.lessonProgressRepository = lessonProgressRepository;

    }

    public List<QuizSummaryResponse> searchQuizzes(
            User user,
            String query) {

        if (query == null
                || query.trim().isEmpty()) {

            return List.of();
        }

        return quizRepository
                .findByTitleContainingIgnoreCase(
                        query.trim())
                .stream()
                .map(quiz -> buildSummary(
                        user,
                        quiz))
                .toList();
    }

    public List<QuizResponse> getQuizzesByLesson(
            User user,
            Long lessonId) {

        List<Quiz> quizzes = quizRepository
                .findByLessonIdOrderByCheckpointOrderAsc(
                        lessonId);

        return quizzes.stream()
                .map(quiz -> buildQuizResponse(
                        user,
                        quiz))
                .toList();
    }

    public QuizResponse getQuiz(
            User user,
            Long quizId) {

        Quiz quiz = getQuizEntity(quizId);

        return buildQuizResponse(
                user,
                quiz);
    }

    @Transactional
    public QuizProgressResponse startQuiz(
            User user,
            Long quizId) {

        Quiz quiz = getQuizEntity(quizId);

        validateQuizUnlocked(
                user,
                quiz);

        QuizProgress progress = quizProgressRepository
                .findByUserIdAndQuizId(
                        user.getId(),
                        quizId)
                .orElse(null);

        if (progress == null) {

            progress = new QuizProgress(
                    user,
                    quiz);
        }

        QuizProgress saved = quizProgressRepository
                .save(progress);

        return new QuizProgressResponse(saved);
    }

    @Transactional
    public QuizAnswerResponse submitAnswer(
            User user,
            Long quizId,
            SubmitQuizAnswerRequest request) {

        Quiz quiz = getQuizEntity(quizId);

        validateQuizUnlocked(
                user,
                quiz);

        QuizProgress progress = quizProgressRepository
                .findByUserIdAndQuizId(
                        user.getId(),
                        quizId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Quiz has not been started"));

        if (progress.isCompleted()) {
            throw new IllegalArgumentException(
                    "Quiz is already completed");
        }

        QuizQuestion question = quizQuestionRepository
                .findById(
                        request.getQuestionId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Quiz question not found"));

        if (!question.getQuiz()
                .getId()
                .equals(quizId)) {

            throw new IllegalArgumentException(
                    "Question does not belong to this quiz");
        }

        if (request.getSelectedAnswer() == null
                || request.getSelectedAnswer().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Selected answer is required");
        }

        if (quizAnswerRepository
                .existsByUserIdAndQuizQuestionId(
                        user.getId(),
                        question.getId())) {

            throw new IllegalArgumentException(
                    "Question has already been answered");
        }

        boolean correct = question.getCorrectAnswer()
                .equalsIgnoreCase(
                        request
                                .getSelectedAnswer()
                                .trim());

        QuizAnswer answer = new QuizAnswer(
                user,
                question,
                request.getSelectedAnswer(),
                correct);

        quizAnswerRepository.save(answer);

        progress.setCurrentQuestion(
                progress.getCurrentQuestion() + 1);

        quizProgressRepository.save(progress);

        return new QuizAnswerResponse(
                question.getId(),
                correct);
    }

    @Transactional
    public QuizProgressResponse completeQuiz(
            User user,
            Long quizId,
            CompleteQuizRequest request) {

        Quiz quiz = getQuizEntity(quizId);

        validateQuizUnlocked(
                user,
                quiz);

        QuizProgress progress = quizProgressRepository
                .findByUserIdAndQuizId(
                        user.getId(),
                        quizId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Quiz has not been started"));

        if (progress.isCompleted()) {

            return new QuizProgressResponse(
                    progress);
        }

        List<QuizQuestion> questions = quizQuestionRepository
                .findByQuizIdOrderByQuestionOrderAsc(
                        quizId);

        List<QuizAnswer> answers = quizAnswerRepository
                .findByUserIdAndQuizQuestionQuizId(
                        user.getId(),
                        quizId);

        if (answers.size() < questions.size()) {

            throw new IllegalArgumentException(
                    "All quiz questions must be answered");
        }

        int correctAnswers = (int) answers.stream()
                .filter(answer -> answer.isCorrect())
                .count();

        int score;

        if (questions.isEmpty()) {

            score = 0;

        } else {

            score = (int) Math.round(
                    (correctAnswers * 100.0)
                            / questions.size());
        }

        progress.setScore(score);
        progress.setCompleted(true);
        progress.setCompletedAt(
                LocalDateTime.now());

        QuizProgress saved = quizProgressRepository
                .save(progress);

        return new QuizProgressResponse(saved);
    }

    public QuizProgressResponse getProgress(
            User user,
            Long quizId) {

        Quiz quiz = getQuizEntity(quizId);

        validateQuizUnlocked(
                user,
                quiz);

        QuizProgress progress = quizProgressRepository
                .findByUserIdAndQuizId(
                        user.getId(),
                        quizId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Quiz has not been started"));

        return new QuizProgressResponse(
                progress);
    }

    private Quiz getQuizEntity(
            Long quizId) {

        return quizRepository
                .findById(quizId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Quiz not found"));
    }

    private void validateQuizUnlocked(
            User user,
            Quiz quiz) {

        LessonProgress progress = lessonProgressRepository
                .findByUserIdAndLessonId(
                        user.getId(),
                        quiz.getLesson().getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Lesson has not been started"));

        if (progress.getCompletedCheckpoints() < quiz.getCheckpointOrder()) {

            throw new IllegalArgumentException(
                    "Complete the required lesson checkpoint before starting this quiz");
        }
    }

    private boolean isLocked(
            User user,
            Quiz quiz) {

        return lessonProgressRepository
                .findByUserIdAndLessonId(
                        user.getId(),
                        quiz.getLesson().getId())
                .map(progress -> progress.getCompletedCheckpoints() < quiz.getCheckpointOrder())
                .orElse(true);
    }

    private QuizSummaryResponse buildSummary(
            User user,
            Quiz quiz) {

        return new QuizSummaryResponse(
                quiz.getId(),
                quiz.getTitle(),
                quiz.getLesson().getId(),
                quiz.getLesson().getTitle(),
                isLocked(user, quiz));
    }

    private QuizResponse buildQuizResponse(
            User user,
            Quiz quiz) {

        boolean locked = isLocked(user, quiz);

        List<QuizQuestionResponse> questions = quizQuestionRepository
                .findByQuizIdOrderByQuestionOrderAsc(
                        quiz.getId())
                .stream()
                .map(
                        QuizQuestionResponse::new)
                .toList();

        return new QuizResponse(
                quiz,
                locked,
                questions);
    }
}