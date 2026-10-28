// Models/Quiz/QuizSummary.swift

import Foundation

/// A quiz without its questions, as returned by search.
struct QuizSummary: Codable, Identifiable, Hashable {
    let id: Int
    let title: String
    let lessonId: Int
    let lessonTitle: String
    let locked: Bool
}
