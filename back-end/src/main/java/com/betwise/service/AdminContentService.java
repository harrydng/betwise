package com.betwise.service;

import com.betwise.dto.admin.AdminDtos.*;
import com.betwise.exception.ConflictException;
import com.betwise.exception.ResourceNotFoundException;
import com.betwise.model.*;
import com.betwise.repository.*;
import com.betwise.util.ChoiceListCodec;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Everything the admin app can change: lesson categories, lessons, their
 * checkpoints, and quizzes. All validation for authored content lives here
 * so the admin app and any future tools get the same rules.
 */
@Service
public class AdminContentService {

    private static final int MAX_TITLE = 255;
    private static final int MAX_CHECKPOINT_TITLE = 150;
    private static final int MAX_CATEGORY_NAME = 100;

    private final LessonCategoryRepository categoryRepository;
    private final LessonRepository lessonRepository;
    private final LessonCheckpointRepository checkpointRepository;
    private final LessonProgressRepository lessonProgressRepository;
    private final QuizRepository quizRepository;
    private final QuizQuestionRepository questionRepository;
    private final QuizAnswerRepository answerRepository;
    private final QuizProgressRepository quizProgressRepository;

    public AdminContentService(
            LessonCategoryRepository categoryRepository,
            LessonRepository lessonRepository,
            LessonCheckpointRepository checkpointRepository,
            LessonProgressRepository lessonProgressRepository,
            QuizRepository quizRepository,
            QuizQuestionRepository questionRepository,
            QuizAnswerRepository answerRepository,
            QuizProgressRepository quizProgressRepository) {

        this.categoryRepository = categoryRepository;
        this.lessonRepository = lessonRepository;
        this.checkpointRepository = checkpointRepository;
        this.lessonProgressRepository = lessonProgressRepository;
        this.quizRepository = quizRepository;
        this.questionRepository = questionRepository;
        this.answerRepository = answerRepository;
        this.quizProgressRepository = quizProgressRepository;
    }

    // MARK: - Categories

    public List<CategoryResponse> getCategories() {
        return categoryRepository.findAll().stream()
                .sorted(Comparator.comparing(LessonCategory::getName, String.CASE_INSENSITIVE_ORDER))
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        String name = required(request.name(), "Category name", MAX_CATEGORY_NAME);
        String slug = uniqueSlug(isBlank(request.slug()) ? name : request.slug(), null);

        LessonCategory category = new LessonCategory(name, slug, trimToNull(request.description()));

        return toResponse(categoryRepository.save(category));
    }

    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        LessonCategory category = getCategory(id);

        if (request.name() != null) {
            category.setName(required(request.name(), "Category name", MAX_CATEGORY_NAME));
        }

        if (request.slug() != null) {
            category.setSlug(uniqueSlug(request.slug(), category));
        }

        if (request.description() != null) {
            category.setDescription(trimToNull(request.description()));
        }

        return toResponse(categoryRepository.save(category));
    }

    @Transactional
    public void deleteCategory(Long id) {
        LessonCategory category = getCategory(id);

        if (lessonRepository.countByCategoryId(id) > 0) {
            throw new ConflictException("Move or delete this category's lessons first");
        }

        categoryRepository.delete(category);
    }

    // MARK: - Lessons

    public List<LessonSummary> getLessons() {
        return lessonRepository.findAllByOrderByPositionAscIdAsc().stream()
                .map(this::toSummary)
                .toList();
    }

    public LessonDetail getLesson(Long id) {
        return toDetail(getLessonEntity(id));
    }

    /** New lessons start as drafts at the end of the list. */
    @Transactional
    public LessonDetail createLesson(LessonRequest request) {
        Lesson lesson = new Lesson();

        lesson.setTitle(required(request.title(), "Title", MAX_TITLE));
        lesson.setDescription(trimToNull(request.description()));
        lesson.setCategory(getCategory(requiredValue(request.categoryId(), "Category")));
        lesson.setTopic(requiredValue(request.topic(), "Topic"));
        lesson.setDifficulty(requiredValue(request.difficulty(), "Difficulty"));
        lesson.setCashReward(reward(request.cashReward() == null ? BigDecimal.ZERO : request.cashReward()));
        lesson.setPosition(request.position() != null ? request.position() : nextLessonPosition());
        lesson.setPublished(false);
        lesson.setArchived(false);

        if (Boolean.TRUE.equals(request.published())) {
            throw new IllegalArgumentException("Add at least one checkpoint before publishing");
        }

        return toDetail(lessonRepository.save(lesson));
    }

    @Transactional
    public LessonDetail updateLesson(Long id, LessonRequest request) {
        Lesson lesson = getLessonEntity(id);

        if (request.title() != null) {
            lesson.setTitle(required(request.title(), "Title", MAX_TITLE));
        }
        if (request.description() != null) {
            lesson.setDescription(trimToNull(request.description()));
        }
        if (request.categoryId() != null) {
            lesson.setCategory(getCategory(request.categoryId()));
        }
        if (request.topic() != null) {
            lesson.setTopic(request.topic());
        }
        if (request.difficulty() != null) {
            lesson.setDifficulty(request.difficulty());
        }
        if (request.cashReward() != null) {
            lesson.setCashReward(reward(request.cashReward()));
        }
        if (request.position() != null) {
            lesson.setPosition(request.position());
        }
        if (request.archived() != null) {
            lesson.setArchived(request.archived());
        }
        if (request.published() != null) {
            if (request.published() && checkpointRepository.countByLessonId(id) == 0) {
                throw new IllegalArgumentException("Add at least one checkpoint before publishing");
            }
            lesson.setPublished(request.published());
        }

        return toDetail(lessonRepository.save(lesson));
    }

    /**
     * Only lessons nobody has started can be deleted; anything else is
     * archived so users keep their progress and rewards.
     */
    @Transactional
    public void deleteLesson(Long id) {
        Lesson lesson = getLessonEntity(id);

        if (lessonProgressRepository.countByLessonId(id) > 0) {
            throw new ConflictException("Learners have started this lesson. Archive it instead.");
        }

        for (Quiz quiz : quizRepository.findByLessonIdOrderByCheckpointOrderAsc(id)) {
            deleteQuizEntity(quiz);
        }

        checkpointRepository.deleteByLessonId(id);
        lessonRepository.delete(lesson);
    }

    // MARK: - Checkpoints

    /**
     * Replaces the lesson's sections with `inputs`, in that order. Sending
     * the full list each time keeps reordering simple for the editor.
     */
    @Transactional
    public List<CheckpointResponse> replaceCheckpoints(Long lessonId, List<CheckpointInput> inputs) {
        Lesson lesson = getLessonEntity(lessonId);
        List<CheckpointInput> sections = inputs == null ? List.of() : inputs;

        if (sections.isEmpty() && lesson.isPublished()) {
            throw new IllegalArgumentException("A published lesson needs at least one checkpoint. Unpublish it first.");
        }

        List<LessonCheckpoint> validated = new ArrayList<>();

        for (int i = 0; i < sections.size(); i++) {
            CheckpointInput input = sections.get(i);
            String label = "Checkpoint " + (i + 1);

            validated.add(new LessonCheckpoint(
                    lesson,
                    i + 1,
                    required(input.title(), label + " title", MAX_CHECKPOINT_TITLE),
                    required(input.body(), label + " content", Integer.MAX_VALUE)));
        }

        for (Quiz quiz : quizRepository.findByLessonIdOrderByCheckpointOrderAsc(lessonId)) {
            if (quiz.getCheckpointOrder() > validated.size()) {
                throw new IllegalArgumentException(
                        "\"" + quiz.getTitle() + "\" unlocks after checkpoint " + quiz.getCheckpointOrder()
                                + ". Keep at least that many checkpoints or change the quiz first.");
            }
        }

        checkpointRepository.deleteByLessonId(lessonId);
        checkpointRepository.flush();
        checkpointRepository.saveAll(validated);

        return checkpointRepository.findByLessonIdOrderByPositionAsc(lessonId).stream()
                .map(this::toResponse)
                .toList();
    }

    // MARK: - Quizzes

    @Transactional
    public QuizResponse createQuiz(Long lessonId, QuizRequest request) {
        Lesson lesson = getLessonEntity(lessonId);

        Quiz quiz = new Quiz(
                lesson,
                required(request.title(), "Quiz title", MAX_TITLE),
                checkpointOrder(lessonId, request.checkpointOrder()));

        quiz = quizRepository.save(quiz);
        saveQuestions(quiz, request.questions());

        return toResponse(quiz);
    }

    @Transactional
    public QuizResponse updateQuiz(Long quizId, QuizRequest request) {
        Quiz quiz = getQuizEntity(quizId);

        if (request.title() != null) {
            quiz.setTitle(required(request.title(), "Quiz title", MAX_TITLE));
        }
        if (request.checkpointOrder() != null) {
            quiz.setCheckpointOrder(checkpointOrder(quiz.getLesson().getId(), request.checkpointOrder()));
        }

        quiz = quizRepository.save(quiz);

        if (request.questions() != null) {
            saveQuestions(quiz, request.questions());
        }

        return toResponse(quiz);
    }

    @Transactional
    public void deleteQuiz(Long quizId) {
        Quiz quiz = getQuizEntity(quizId);

        if (quizProgressRepository.existsByQuizId(quizId) || answerRepository.existsByQuizQuestionQuizId(quizId)) {
            throw new ConflictException("Learners have taken this quiz, so it can't be deleted");
        }

        deleteQuizEntity(quiz);
    }

    /**
     * Updates, adds, reorders and removes questions to match `inputs`.
     * Questions people have already answered can be edited but not removed.
     */
    private void saveQuestions(Quiz quiz, List<QuestionInput> inputs) {
        if (inputs == null || inputs.isEmpty()) {
            throw new IllegalArgumentException("A quiz needs at least one question");
        }

        Map<Long, QuizQuestion> existing = questionRepository
                .findByQuizIdOrderByQuestionOrderAsc(quiz.getId()).stream()
                .collect(Collectors.toMap(QuizQuestion::getId, Function.identity()));

        Set<Long> kept = new HashSet<>();
        List<QuizQuestion> toSave = new ArrayList<>();

        for (int i = 0; i < inputs.size(); i++) {
            QuestionInput input = inputs.get(i);
            String label = "Question " + (i + 1);

            QuizQuestion question;

            if (input.id() != null) {
                question = existing.get(input.id());

                if (question == null) {
                    throw new IllegalArgumentException(label + " doesn't belong to this quiz");
                }

                kept.add(input.id());
            } else {
                question = new QuizQuestion();
                question.setQuiz(quiz);
                question.setCashReward(BigDecimal.ZERO);
            }

            List<String> choices = validatedChoices(input.choices(), label);

            question.setQuestion(required(input.question(), label, Integer.MAX_VALUE));
            question.setChoices(ChoiceListCodec.encode(choices));
            question.setCorrectAnswer(matchingChoice(choices, input.correctAnswer(), label));
            question.setQuestionOrder(i + 1);

            toSave.add(question);
        }

        for (QuizQuestion removed : existing.values()) {
            if (kept.contains(removed.getId())) {
                continue;
            }

            if (answerRepository.existsByQuizQuestionId(removed.getId())) {
                throw new ConflictException(
                        "\"" + removed.getQuestion() + "\" has already been answered, so it can't be removed");
            }

            questionRepository.delete(removed);
        }

        questionRepository.saveAll(toSave);
    }

    private static List<String> validatedChoices(List<String> rawChoices, String label) {
        List<String> choices = rawChoices == null ? List.of() : rawChoices.stream()
                .map(choice -> choice == null ? "" : choice.trim())
                .filter(choice -> !choice.isEmpty())
                .toList();

        if (choices.size() < 2) {
            throw new IllegalArgumentException(label + " needs at least two choices");
        }

        Set<String> seen = new HashSet<>();

        for (String choice : choices) {
            // Answers are checked case-insensitively, so duplicates that only
            // differ in case would be ambiguous.
            if (!seen.add(choice.toLowerCase(Locale.ROOT))) {
                throw new IllegalArgumentException(label + " has the choice \"" + choice + "\" twice");
            }
        }

        return choices;
    }

    private static String matchingChoice(List<String> choices, String correctAnswer, String label) {
        if (isBlank(correctAnswer)) {
            throw new IllegalArgumentException(label + " needs a correct answer");
        }

        return choices.stream()
                .filter(choice -> choice.equalsIgnoreCase(correctAnswer.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        label + "'s correct answer must be one of its choices"));
    }

    /** Quizzes unlock after a checkpoint, so it has to exist. */
    private int checkpointOrder(Long lessonId, Integer requested) {
        int order = requested == null ? 1 : requested;
        long checkpointCount = checkpointRepository.countByLessonId(lessonId);

        if (order < 1) {
            throw new IllegalArgumentException("Quizzes unlock after checkpoint 1 or later");
        }

        if (order > checkpointCount) {
            throw new IllegalArgumentException(
                    checkpointCount == 0
                            ? "Add checkpoints to this lesson before adding a quiz"
                            : "This lesson only has " + checkpointCount + " checkpoints");
        }

        return order;
    }

    private void deleteQuizEntity(Quiz quiz) {
        questionRepository.deleteAll(questionRepository.findByQuizIdOrderByQuestionOrderAsc(quiz.getId()));
        quizRepository.delete(quiz);
    }

    // MARK: - Lookups

    private LessonCategory getCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
    }

    private Lesson getLessonEntity(Long id) {
        return lessonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found"));
    }

    private Quiz getQuizEntity(Long id) {
        return quizRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found"));
    }

    private int nextLessonPosition() {
        return lessonRepository.findAll().stream()
                .mapToInt(Lesson::getPosition)
                .max()
                .orElse(0) + 1;
    }

    // MARK: - Mapping

    private CategoryResponse toResponse(LessonCategory category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getDescription(),
                lessonRepository.countByCategoryId(category.getId()));
    }

    private LessonSummary toSummary(Lesson lesson) {
        return new LessonSummary(
                lesson.getId(),
                lesson.getTitle(),
                lesson.getCategory().getId(),
                lesson.getCategory().getName(),
                lesson.getTopic(),
                lesson.getDifficulty(),
                lesson.getCashReward(),
                lesson.getPosition(),
                lesson.isPublished(),
                lesson.isArchived(),
                checkpointRepository.countByLessonId(lesson.getId()),
                quizRepository.countByLessonId(lesson.getId()),
                lessonProgressRepository.countByLessonId(lesson.getId()),
                lesson.getUpdatedAt());
    }

    private LessonDetail toDetail(Lesson lesson) {
        return new LessonDetail(
                toSummary(lesson),
                lesson.getDescription(),
                checkpointRepository.findByLessonIdOrderByPositionAsc(lesson.getId()).stream()
                        .map(this::toResponse)
                        .toList(),
                quizRepository.findByLessonIdOrderByCheckpointOrderAsc(lesson.getId()).stream()
                        .map(this::toResponse)
                        .toList());
    }

    private CheckpointResponse toResponse(LessonCheckpoint checkpoint) {
        return new CheckpointResponse(
                checkpoint.getId(),
                checkpoint.getPosition(),
                checkpoint.getTitle(),
                checkpoint.getBody());
    }

    private QuizResponse toResponse(Quiz quiz) {
        return new QuizResponse(
                quiz.getId(),
                quiz.getTitle(),
                quiz.getCheckpointOrder(),
                quizProgressRepository.existsByQuizId(quiz.getId()),
                questionRepository.findByQuizIdOrderByQuestionOrderAsc(quiz.getId()).stream()
                        .map(question -> new QuestionResponse(
                                question.getId(),
                                question.getQuestionOrder(),
                                question.getQuestion(),
                                ChoiceListCodec.decode(question.getChoices()),
                                question.getCorrectAnswer()))
                        .toList());
    }

    // MARK: - Validation helpers

    private static String required(String value, String field, int maxLength) {
        if (isBlank(value)) {
            throw new IllegalArgumentException(field + " is required");
        }

        String trimmed = value.trim();

        if (trimmed.length() > maxLength) {
            throw new IllegalArgumentException(field + " must be " + maxLength + " characters or fewer");
        }

        return trimmed;
    }

    private static <T> T requiredValue(T value, String field) {
        if (value == null) {
            throw new IllegalArgumentException(field + " is required");
        }

        return value;
    }

    private static BigDecimal reward(BigDecimal value) {
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Reward can't be negative");
        }

        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private String uniqueSlug(String source, LessonCategory current) {
        String slug = source.trim().toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-+|-+$)", "");

        if (slug.isEmpty()) {
            throw new IllegalArgumentException("Slug must contain letters or numbers");
        }

        boolean unchanged = current != null && slug.equalsIgnoreCase(current.getSlug());

        if (!unchanged && categoryRepository.existsBySlugIgnoreCase(slug)) {
            throw new ConflictException("Another category already uses the slug \"" + slug + "\"");
        }

        return slug;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static String trimToNull(String value) {
        return isBlank(value) ? null : value.trim();
    }
}
