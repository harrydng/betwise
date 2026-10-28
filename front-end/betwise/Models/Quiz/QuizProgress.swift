// Models/Quiz/QuizProgress.swift

import Foundation

struct QuizProgress: Codable, Hashable {
    let quizId: Int
    let currentQuestion: Int
    let score: Int
    let completed: Bool
}
