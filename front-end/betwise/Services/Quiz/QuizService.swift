// Services/Quiz/QuizService.swift

import Foundation

final class QuizService {
    static let shared = QuizService()

    private let api = APIClient.shared

    private init() {}

    func searchQuizzes(
        query: String
    ) async throws -> [QuizSummary] {
        try await api.request(
            .get,
            "/api/quizzes/search",
            query: [URLQueryItem(name: "query", value: query)]
        )
    }

    func getQuiz(
        id: Int
    ) async throws -> Quiz {
        try await api.request(
            .get,
            "/api/quizzes/\(id)"
        )
    }

    /// Fails if the quiz is still locked.
    func startQuiz(
        id: Int
    ) async throws -> QuizProgress {
        try await api.request(
            .post,
            "/api/quizzes/\(id)/start"
        )
    }

    func submitAnswer(
        quizId: Int,
        questionId: Int,
        selectedAnswer: String
    ) async throws -> QuizAnswerResult {
        try await api.request(
            .post,
            "/api/quizzes/\(quizId)/answers",
            body: SubmitQuizAnswerRequest(
                questionId: questionId,
                selectedAnswer: selectedAnswer
            )
        )
    }

    func getProgress(
        quizId: Int
    ) async throws -> QuizProgress {
        try await api.request(
            .get,
            "/api/quizzes/\(quizId)/progress"
        )
    }

    /// Scores the quiz once every question has been answered.
    func completeQuiz(
        id: Int,
        portfolioId: Int? = nil
    ) async throws -> QuizProgress {
        try await api.request(
            .post,
            "/api/quizzes/\(id)/complete",
            body: CompleteQuizRequest(
                portfolioId: portfolioId
            )
        )
    }
}
