// Models/Quiz/CompleteQuizRequest.swift

import Foundation

/// Quizzes don't pay out yet, so `portfolioId` may be left out.
struct CompleteQuizRequest: Codable {
    var portfolioId: Int?
}
