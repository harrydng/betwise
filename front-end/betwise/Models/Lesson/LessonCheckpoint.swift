// Models/Lesson/LessonCheckpoint.swift

import Foundation

/// One section of a lesson, read in `position` order.
struct LessonCheckpoint: Codable, Identifiable, Hashable {
    let id: Int

    /// 1-based, matching `LessonProgress.currentCheckpoint`.
    let position: Int

    let title: String

    /// Markdown. Inline styles render, and line breaks are kept.
    let body: String
}
