// Models/Quiz/SubmitQuizAnswerRequest.swift

import Foundation

struct SubmitQuizAnswerRequest: Codable {
    let questionId: Int

    /// Must match the stored correct answer's text. The server compares
    /// strings, so send one of `QuizQuestion.choiceList` as-is.
    let selectedAnswer: String
}
