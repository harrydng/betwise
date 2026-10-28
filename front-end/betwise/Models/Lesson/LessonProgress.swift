// Models/Lesson/LessonProgress.swift

import Foundation

struct LessonProgress: Codable, Hashable {
    let lessonId: Int
    let completed: Bool
    let currentCheckpoint: Int
    let completedCheckpoints: Int
    let startedAt: Date
    let lastAccessedAt: Date
    let completedAt: Date?
}
