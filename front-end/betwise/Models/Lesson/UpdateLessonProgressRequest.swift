// Models/Lesson/UpdateLessonProgressRequest.swift

import Foundation

/// Leave a field `nil` to keep its current value.
struct UpdateLessonProgressRequest: Codable {
    var currentCheckpoint: Int?
    var completedCheckpoints: Int?
}
