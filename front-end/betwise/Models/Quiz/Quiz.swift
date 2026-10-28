// Models/Quiz/Quiz.swift

import Foundation

struct Quiz: Codable, Identifiable, Hashable {
    let id: Int
    let title: String
    let lessonId: Int
    let lessonTitle: String
    let locked: Bool
    let questions: [QuizQuestion]
}
