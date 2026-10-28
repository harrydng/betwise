package com.betwise.service;

import com.betwise.dto.admin.AdminDtos.*;
import com.betwise.exception.ConflictException;
import com.betwise.model.*;
import com.betwise.repository.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminContentServiceTest {

    @Mock private LessonCategoryRepository categoryRepository;
    @Mock private LessonRepository lessonRepository;
    @Mock private LessonCheckpointRepository checkpointRepository;
    @Mock private LessonProgressRepository lessonProgressRepository;
    @Mock private QuizRepository quizRepository;
    @Mock private QuizQuestionRepository questionRepository;
    @Mock private QuizAnswerRepository answerRepository;
    @Mock private QuizProgressRepository quizProgressRepository;

    @InjectMocks
    private AdminContentService service;

    // MARK: - Categories

    @Test
    void categorySlugIsDerivedFromTheName() {

        when(categoryRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CategoryResponse response = service.createCategory(
                new CategoryRequest("Stocks & Funds!", null, null));

        assertEquals("stocks-funds", response.slug());
    }

    @Test
    void duplicateCategorySlugIsRejected() {

        when(categoryRepository.existsBySlugIgnoreCase("basics")).thenReturn(true);

        assertThrows(ConflictException.class, () ->
                service.createCategory(new CategoryRequest("Basics", null, null)));
    }

    // MARK: - Lessons

    @Test
    void lessonCantBePublishedWithoutCheckpoints() {

        Lesson lesson = lesson(1L);
        when(lessonRepository.findById(1L)).thenReturn(Optional.of(lesson));
        when(checkpointRepository.countByLessonId(1L)).thenReturn(0L);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () ->
                service.updateLesson(1L, new LessonRequest(
                        null, null, null, null, null, null, null, true, null)));

        assertEquals("Add at least one checkpoint before publishing", error.getMessage());
        assertFalse(lesson.isPublished());
    }

    @Test
    void negativeRewardIsRejected() {

        Lesson lesson = lesson(1L);

        when(lessonRepository.findById(1L)).thenReturn(Optional.of(lesson));

        assertThrows(IllegalArgumentException.class, () ->
                service.updateLesson(1L, new LessonRequest(
                        null, null, null, null, null, new BigDecimal("-5"), null, null, null)));
    }

    @Test
    void startedLessonCantBeDeleted() {

        Lesson lesson = lesson(1L);

        when(lessonRepository.findById(1L)).thenReturn(Optional.of(lesson));
        when(lessonProgressRepository.countByLessonId(1L)).thenReturn(3L);

        assertThrows(ConflictException.class, () -> service.deleteLesson(1L));

        verify(lessonRepository, never()).delete(any());
    }

    // MARK: - Checkpoints

    @Test
    void checkpointsCantDropBelowAQuizsUnlockPoint() {

        Lesson lesson = lesson(1L);
        Quiz quiz = new Quiz(lesson, "Final check", 3);

        when(lessonRepository.findById(1L)).thenReturn(Optional.of(lesson));
        when(quizRepository.findByLessonIdOrderByCheckpointOrderAsc(1L)).thenReturn(List.of(quiz));

        assertThrows(IllegalArgumentException.class, () ->
                service.replaceCheckpoints(1L, List.of(
                        new CheckpointInput("One", "Body"),
                        new CheckpointInput("Two", "Body"))));

        verify(checkpointRepository, never()).deleteByLessonId(any());
    }

    @Test
    void checkpointWithoutContentIsRejected() {

        Lesson lesson = lesson(1L);

        when(lessonRepository.findById(1L)).thenReturn(Optional.of(lesson));

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () ->
                service.replaceCheckpoints(1L, List.of(new CheckpointInput("Intro", "  "))));

        assertEquals("Checkpoint 1 content is required", error.getMessage());
    }

    // MARK: - Quizzes

    @Test
    void correctAnswerMustBeOneOfTheChoices() {

        stubQuizCreation();

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () ->
                service.createQuiz(1L, new QuizRequest("Check", 1, List.of(
                        new QuestionInput(null, "2 + 2?", List.of("3", "4"), "5")))));

        assertEquals("Question 1's correct answer must be one of its choices", error.getMessage());
    }

    @Test
    void questionNeedsTwoDistinctChoices() {

        stubQuizCreation();

        assertThrows(IllegalArgumentException.class, () ->
                service.createQuiz(1L, new QuizRequest("Check", 1, List.of(
                        new QuestionInput(null, "Pick one", List.of("Yes", " yes "), "Yes")))));
    }

    @Test
    void validQuizStoresChoicesAsJsonAndNormalizesTheAnswer() {

        stubQuizCreation();

        service.createQuiz(1L, new QuizRequest("Check", 1, List.of(
                new QuestionInput(null, " What's an ETF? ", List.of(" A fund ", "A bond"), "a fund"))));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<QuizQuestion>> saved = ArgumentCaptor.forClass(List.class);
        verify(questionRepository).saveAll(saved.capture());

        QuizQuestion question = saved.getValue().get(0);

        assertEquals("What's an ETF?", question.getQuestion());
        assertEquals("[\"A fund\",\"A bond\"]", question.getChoices());
        assertEquals("A fund", question.getCorrectAnswer());
        assertEquals(1, question.getQuestionOrder());
    }

    @Test
    void quizCantUnlockAfterACheckpointThatDoesntExist() {

        Lesson lesson = lesson(1L);

        when(lessonRepository.findById(1L)).thenReturn(Optional.of(lesson));
        when(checkpointRepository.countByLessonId(1L)).thenReturn(2L);

        assertThrows(IllegalArgumentException.class, () ->
                service.createQuiz(1L, new QuizRequest("Check", 5, List.of(
                        new QuestionInput(null, "Q", List.of("A", "B"), "A")))));
    }

    @Test
    void takenQuizCantBeDeleted() {

        Quiz quiz = mock(Quiz.class);
        when(quizRepository.findById(9L)).thenReturn(Optional.of(quiz));
        when(quizProgressRepository.existsByQuizId(9L)).thenReturn(true);

        assertThrows(ConflictException.class, () -> service.deleteQuiz(9L));

        verify(quizRepository, never()).delete(any());
    }

    // MARK: - Helpers

    private void stubQuizCreation() {
        Lesson lesson = lesson(1L);

        when(lessonRepository.findById(1L)).thenReturn(Optional.of(lesson));
        when(checkpointRepository.countByLessonId(1L)).thenReturn(3L);
        when(quizRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(questionRepository.findByQuizIdOrderByQuestionOrderAsc(any())).thenReturn(List.of());
        lenient().when(questionRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private static Lesson lesson(Long id) {
        Lesson lesson = mock(Lesson.class, withSettings().useConstructor().defaultAnswer(CALLS_REAL_METHODS));
        lenient().when(lesson.getId()).thenReturn(id);
        lesson.setTitle("Lesson " + id);
        lesson.setCategory(new LessonCategory("Basics", "basics", null));
        lesson.setTopic(Lesson.Topic.STOCKS);
        lesson.setDifficulty(Lesson.Difficulty.BEGINNER);
        lesson.setCashReward(BigDecimal.TEN);
        return lesson;
    }
}
