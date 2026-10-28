// Services/Lesson/LessonService.swift

import Foundation

final class LessonService {
    static let shared = LessonService()

    private let api = APIClient.shared

    private init() {}

    func getCategories() async throws -> [LessonCategory] {
        try await api.request(
            .get,
            "/api/lesson-categories"
        )
    }

    /// Pass a `LessonCategory.slug` to filter, or `nil` for every lesson.
    func getLessons(
        categorySlug: String? = nil
    ) async throws -> [Lesson] {
        var query: [URLQueryItem] = []

        if let categorySlug {
            query.append(URLQueryItem(name: "category", value: categorySlug))
        }

        return try await api.request(
            .get,
            "/api/lessons",
            query: query
        )
    }

    func searchLessons(
        query: String
    ) async throws -> [Lesson] {
        try await api.request(
            .get,
            "/api/lessons/search",
            query: [URLQueryItem(name: "query", value: query)]
        )
    }

    func getLesson(
        id: Int
    ) async throws -> Lesson {
        try await api.request(
            .get,
            "/api/lessons/\(id)"
        )
    }

    func startLesson(
        id: Int
    ) async throws -> LessonProgress {
        try await api.request(
            .post,
            "/api/lessons/\(id)/start"
        )
    }

    func getProgress(
        lessonId: Int
    ) async throws -> LessonProgress {
        try await api.request(
            .get,
            "/api/lessons/\(lessonId)/progress"
        )
    }

    func updateProgress(
        lessonId: Int,
        currentCheckpoint: Int? = nil,
        completedCheckpoints: Int? = nil
    ) async throws -> LessonProgress {
        try await api.request(
            .patch,
            "/api/lessons/\(lessonId)/progress",
            body: UpdateLessonProgressRequest(
                currentCheckpoint: currentCheckpoint,
                completedCheckpoints: completedCheckpoints
            )
        )
    }

    /// Completes the lesson and pays its cash reward into `portfolioId`.
    func completeLesson(
        id: Int,
        portfolioId: Int
    ) async throws -> LessonProgress {
        try await api.request(
            .post,
            "/api/lessons/\(id)/complete",
            body: CompleteLessonRequest(
                portfolioId: portfolioId
            )
        )
    }

    /// The lesson's sections, in reading order.
    func getCheckpoints(
        lessonId: Int
    ) async throws -> [LessonCheckpoint] {
        try await api.request(
            .get,
            "/api/lessons/\(lessonId)/checkpoints"
        )
    }

    func getQuizzes(
        lessonId: Int
    ) async throws -> [Quiz] {
        try await api.request(
            .get,
            "/api/lessons/\(lessonId)/quizzes"
        )
    }
}
