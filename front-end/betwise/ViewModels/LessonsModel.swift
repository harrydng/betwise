// ViewModels/LessonsModel.swift

import Foundation
import Observation

@Observable
final class LessonsModel {
    private(set) var categories: [LessonCategory] = []

    private var lessons: [Lesson] = [] {
        didSet { rebuildRows() }
    }

    private var progress: [Int: LessonProgress] = [:] {
        didSet { rebuildRows() }
    }

    /// The selected category slug, or `nil` for every lesson.
    var selectedCategory: String? {
        didSet { rebuildVisibleRows() }
    }

    // Derived values, cached so views only read prepared data.
    private(set) var rows: [LessonRowItem] = []
    private(set) var visibleRows: [LessonRowItem] = []
    private(set) var rowsByID: [Int: LessonRowItem] = [:]
    private(set) var continueLesson: LessonRowItem?
    private(set) var completedCount = 0
    private(set) var earnedRewards: Decimal = 0
    private(set) var remainingRewards: Decimal = 0

    private(set) var errorMessage: String?

    var categoryOptions: [FilterOption<String?>] {
        [FilterOption(value: nil, title: String(localized: "All"))]
            + categories.map { FilterOption(value: $0.slug, title: $0.name) }
    }

    /// Guests see sample lessons; signed-in users only see the backend's.
    let usesSampleData: Bool

    private(set) var hasLoaded: Bool

    init(usesSampleData: Bool) {
        self.usesSampleData = usesSampleData
        hasLoaded = usesSampleData

        if usesSampleData {
            categories = SampleData.lessonCategories
            lessons = SampleData.lessons
            progress = SampleData.lessonProgress
        }

        // Property observers don't run during init.
        rebuildRows()
    }

    func refresh() async {
        guard !usesSampleData else { return }

        defer { hasLoaded = true }

        do {
            categories = try await LessonService.shared.getCategories()
            let lessons = try await LessonService.shared.getLessons()

            // The API returns progress per lesson, and a 400 for lessons
            // that haven't been started, so treat failures as "not started".
            var progress: [Int: LessonProgress] = [:]

            for lesson in lessons {
                progress[lesson.id] = try? await LessonService.shared.getProgress(lessonId: lesson.id)
            }

            self.lessons = lessons
            self.progress = progress
            errorMessage = nil
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    func start(lessonID: Int) async throws {
        let started = try await LessonService.shared.startLesson(id: lessonID)
        progress[lessonID] = started
    }

    func progress(for lessonID: Int) -> LessonProgress? {
        progress[lessonID]
    }

    func checkpoints(for lessonID: Int) async throws -> [LessonCheckpoint] {
        try await LessonService.shared.getCheckpoints(lessonId: lessonID)
    }

    func quizzes(for lessonID: Int) async throws -> [Quiz] {
        try await LessonService.shared.getQuizzes(lessonId: lessonID)
    }

    /// Records that the reader is now on `currentCheckpoint`. Completed
    /// checkpoints only ever go up, so going back to reread doesn't undo
    /// progress.
    func saveProgress(lessonID: Int, currentCheckpoint: Int, completedCheckpoints: Int) async throws {
        let previouslyCompleted = progress[lessonID]?.completedCheckpoints ?? 0

        progress[lessonID] = try await LessonService.shared.updateProgress(
            lessonId: lessonID,
            currentCheckpoint: currentCheckpoint,
            completedCheckpoints: max(previouslyCompleted, completedCheckpoints)
        )
    }

    /// Finishes the lesson and pays its reward into `portfolioID`.
    func complete(lessonID: Int, portfolioID: Int) async throws {
        progress[lessonID] = try await LessonService.shared.completeLesson(
            id: lessonID,
            portfolioId: portfolioID
        )
    }

    // MARK: - Derived data

    private func rebuildRows() {
        rows = lessons.map { lesson in
            LessonRowItem(
                id: lesson.id,
                title: lesson.title,
                summary: lesson.description,
                category: lesson.category,
                categorySlug: lesson.categorySlug,
                topic: lesson.topic,
                difficulty: lesson.difficulty,
                reward: lesson.cashReward,
                status: Self.status(for: progress[lesson.id]),
                checkpointCount: lesson.checkpointCount ?? 0,
                completedCheckpoints: progress[lesson.id]?.completedCheckpoints ?? 0
            )
        }

        rowsByID = Dictionary(uniqueKeysWithValues: rows.map { ($0.id, $0) })

        let completed = rows.filter { $0.status == .completed }
        completedCount = completed.count
        earnedRewards = completed.reduce(Decimal(0)) { $0 + $1.reward }
        remainingRewards = rows.reduce(Decimal(0)) { $0 + $1.reward } - earnedRewards

        // Resume the most recently opened lesson that isn't finished.
        continueLesson = lessons
            .compactMap { lesson -> (LessonProgress, Int)? in
                guard let progress = progress[lesson.id], !progress.completed else { return nil }
                return (progress, lesson.id)
            }
            .max { $0.0.lastAccessedAt < $1.0.lastAccessedAt }
            .flatMap { rowsByID[$0.1] }

        rebuildVisibleRows()
    }

    private func rebuildVisibleRows() {
        if let selectedCategory {
            visibleRows = rows.filter { $0.categorySlug == selectedCategory }
        } else {
            visibleRows = rows
        }
    }

    private static func status(for progress: LessonProgress?) -> LessonStatus {
        guard let progress else { return .notStarted }

        return progress.completed
            ? .completed
            : .inProgress(checkpoint: progress.currentCheckpoint)
    }
}
