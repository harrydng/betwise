// Models/Notification/AppNotification.swift

import Foundation

/// Named to avoid clashing with Foundation's `Notification`.
struct AppNotification: Codable, Identifiable, Hashable {
    let id: Int
    let type: NotificationType
    let title: String
    let body: String?

    /// The id of what the notification is about. Its meaning depends on
    /// `type`: a transaction, a portfolio, a lesson or a quiz.
    let relatedId: Int?

    let read: Bool
    let readAt: Date?
    let createdAt: Date

    enum NotificationType: String, Codable, CaseIterable {
        case tradeFilled = "TRADE_FILLED"
        case cashTransfer = "CASH_TRANSFER"
        case lessonReward = "LESSON_REWARD"
        case quizCompleted = "QUIZ_COMPLETED"
    }
}

struct UnreadCountResponse: Codable {
    let count: Int
}
