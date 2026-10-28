// Models/Lesson/LessonRowItem.swift

import Foundation

enum LessonStatus: Hashable {
    case notStarted
    case inProgress(checkpoint: Int)
    case completed
}

/// A lesson combined with the user's progress, ready to display.
struct LessonRowItem: Identifiable, Hashable {
    let id: Int
    let title: String
    let summary: String?
    let category: String
    let categorySlug: String
    let topic: Lesson.Topic
    let difficulty: Lesson.Difficulty
    let reward: Decimal
    let status: LessonStatus
    let checkpointCount: Int
    let completedCheckpoints: Int
}
