// Models/Lesson/LessonCategory.swift

import Foundation

struct LessonCategory: Codable, Identifiable, Hashable {
    let id: Int
    let name: String

    /// Pass this to `LessonService.getLessons(categorySlug:)` to filter.
    let slug: String

    let description: String?
    let createdAt: Date
}
