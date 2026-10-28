// Models/Lesson/CompleteLessonRequest.swift

import Foundation

/// `portfolioId` is the portfolio that receives the lesson's cash reward.
struct CompleteLessonRequest: Codable {
    let portfolioId: Int
}
