// ViewModels/NotificationsModel.swift

import Foundation
import Observation

@Observable
final class NotificationsModel {
    private(set) var notifications: [AppNotification] = [] {
        didSet { unreadCount = notifications.count { !$0.read } }
    }

    private(set) var unreadCount = 0
    private(set) var errorMessage: String?

    private let usesSampleData: Bool

    init(usesSampleData: Bool) {
        self.usesSampleData = usesSampleData

        if usesSampleData {
            notifications = SampleData.notifications
            unreadCount = notifications.count { !$0.read }
        }
    }

    func refresh() async {
        guard !usesSampleData else { return }

        do {
            notifications = try await NotificationService.shared.getNotifications()
            errorMessage = nil
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    /// Updates only the badge, for screens that don't show the list.
    func refreshUnreadCount() async {
        guard !usesSampleData else { return }

        if let count = try? await NotificationService.shared.getUnreadCount() {
            unreadCount = count
        }
    }

    func markAsRead(_ notification: AppNotification) async {
        guard !notification.read else { return }

        guard !usesSampleData else {
            replace(notification.markedRead())
            return
        }

        do {
            replace(try await NotificationService.shared.markAsRead(id: notification.id))
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    func markAllAsRead() async {
        guard unreadCount > 0 else { return }

        let previous = notifications
        notifications = notifications.map { $0.markedRead() }

        guard !usesSampleData else { return }

        do {
            try await NotificationService.shared.markAllAsRead()
        } catch {
            notifications = previous
            errorMessage = error.localizedDescription
        }
    }

    private func replace(_ updated: AppNotification) {
        if let index = notifications.firstIndex(where: { $0.id == updated.id }) {
            notifications[index] = updated
        }
    }
}

private extension AppNotification {
    func markedRead() -> AppNotification {
        AppNotification(
            id: id,
            type: type,
            title: title,
            body: body,
            relatedId: relatedId,
            read: true,
            readAt: readAt ?? .now,
            createdAt: createdAt
        )
    }
}
