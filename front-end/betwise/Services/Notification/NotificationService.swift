// Services/Notification/NotificationService.swift

import Foundation

final class NotificationService {
    static let shared = NotificationService()

    private let api = APIClient.shared

    private init() {}

    func getNotifications() async throws -> [AppNotification] {
        try await api.request(
            .get,
            "/api/notifications"
        )
    }

    func getUnreadCount() async throws -> Int {
        let response: UnreadCountResponse = try await api.request(
            .get,
            "/api/notifications/unread-count"
        )

        return response.count
    }

    func markAsRead(
        id: Int
    ) async throws -> AppNotification {
        try await api.request(
            .patch,
            "/api/notifications/\(id)/read"
        )
    }

    func markAllAsRead() async throws {
        try await api.perform(
            .post,
            "/api/notifications/read-all"
        )
    }
}
