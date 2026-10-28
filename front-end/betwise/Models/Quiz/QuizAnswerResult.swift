// Models/Quiz/QuizAnswerResult.swift

import Foundation

struct QuizAnswerResult: Codable, Hashable {
    let questionId: Int
    let correct: Bool
}
