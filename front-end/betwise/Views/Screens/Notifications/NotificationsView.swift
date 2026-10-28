import SwiftUI

struct NotificationsView: View {
    let model: NotificationsModel

    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
            Group {
                if model.notifications.isEmpty {
                    ContentUnavailableView(
                        "No notifications",
                        systemImage: "bell",
                        description: Text("You'll be notified here when orders fill and lesson rewards land.")
                    )
                } else {
                    List {
                        ForEach(model.notifications) { notification in
                            Button {
                                Task {
                                    await model.markAsRead(notification)
                                }
                            } label: {
                                NotificationRow(
                                    type: notification.type,
                                    title: notification.title,
                                    message: notification.body,
                                    createdAt: notification.createdAt,
                                    isRead: notification.read
                                )
                            }
                            .buttonStyle(.plain)
                            .listRowBackground(AppColors.surface)
                            .swipeActions(edge: .leading) {
                                if !notification.read {
                                    Button {
                                        Task {
                                            await model.markAsRead(notification)
                                        }
                                    } label: {
                                        Label("Mark as read", systemImage: "envelope.open")
                                    }
                                    .tint(AppColors.accent)
                                }
                            }
                        }
                    }
                    .scrollContentBackground(.hidden)
                }
            }
            .screenBackground()
            .navigationTitle("Notifications")
            #if os(iOS)
            .navigationBarTitleDisplayMode(.inline)
            #endif
            .toolbar {
                ToolbarItem(placement: .confirmationAction) {
                    Button("Done") {
                        dismiss()
                    }
                }

                ToolbarItem(placement: .primaryAction) {
                    Button("Mark all as read") {
                        Task {
                            await model.markAllAsRead()
                        }
                    }
                    .disabled(model.unreadCount == 0)
                }
            }
            .refreshable {
                await model.refresh()
            }
            .task {
                await model.refresh()
            }
        }
        .presentationBackground(AppColors.background)
    }
}

private struct NotificationRow: View {
    let type: AppNotification.NotificationType
    let title: String
    let message: String?
    let createdAt: Date
    let isRead: Bool

    var body: some View {
        HStack(alignment: .top, spacing: 14) {
            Image(systemName: type.symbolName)
                .font(.subheadline.weight(.semibold))
                .foregroundStyle(AppColors.accentBright)
                .frame(width: 40, height: 40)
                .background(AppColors.accent.opacity(0.16), in: Circle())
                .accessibilityHidden(true)

            VStack(alignment: .leading, spacing: 4) {
                HStack(alignment: .firstTextBaseline) {
                    Text(title)
                        .font(.subheadline.weight(isRead ? .medium : .bold))
                        .foregroundStyle(AppColors.textPrimary)

                    Spacer(minLength: 8)

                    Text(createdAt, format: .relative(presentation: .named))
                        .font(.caption)
                        .foregroundStyle(AppColors.textTertiary)
                }

                if let message {
                    Text(message)
                        .font(.subheadline)
                        .foregroundStyle(isRead ? AppColors.textSecondary : AppColors.textPrimary.opacity(0.85))
                        .fixedSize(horizontal: false, vertical: true)
                }
            }

            Circle()
                .fill(AppColors.accent)
                .frame(width: 8, height: 8)
                .padding(.top, 6)
                .opacity(isRead ? 0 : 1)
                .accessibilityHidden(true)
        }
        .padding(.vertical, 6)
        .contentShape(.rect)
        .accessibilityElement(children: .combine)
        .accessibilityValue(isRead ? Text("") : Text("Unread"))
    }
}

#Preview {
    NotificationsView(model: NotificationsModel(usesSampleData: true))
        .preferredColorScheme(.dark)
}
