import SwiftUI

struct ChangePasswordView: View {
    let session: SessionModel

    @Environment(\.dismiss) private var dismiss

    @State private var currentPassword = ""
    @State private var newPassword = ""
    @State private var confirmation = ""
    @State private var isSaving = false
    @State private var didSave = false
    @State private var errorMessage: String?

    private static let minimumPasswordLength = 8

    var body: some View {
        // Accounts created with Google don't have a password to confirm.
        let needsCurrentPassword = session.user?.hasPassword != false
        let isLongEnough = newPassword.count >= Self.minimumPasswordLength
        let matches = !confirmation.isEmpty && confirmation == newPassword
        let canSave = (!needsCurrentPassword || !currentPassword.isEmpty) && isLongEnough && matches && !isSaving

        ScrollView {
            VStack(alignment: .leading, spacing: 20) {
                if needsCurrentPassword {
                    FormField(title: "Current password", text: $currentPassword, kind: .password)
                } else {
                    Text("You sign in with Google. Add a password to also sign in with your email.")
                        .font(.subheadline)
                        .foregroundStyle(AppColors.textSecondary)
                        .fixedSize(horizontal: false, vertical: true)
                }

                VStack(alignment: .leading, spacing: 6) {
                    FormField(title: "New password", text: $newPassword, kind: .newPassword)

                    Text("At least \(Self.minimumPasswordLength) characters")
                        .font(.caption)
                        .foregroundStyle(newPassword.isEmpty || isLongEnough ? AppColors.textTertiary : AppColors.loss)
                }

                VStack(alignment: .leading, spacing: 6) {
                    FormField(title: "Confirm new password", text: $confirmation, kind: .newPassword)

                    if !confirmation.isEmpty && !matches {
                        Text("Passwords don't match")
                            .font(.caption)
                            .foregroundStyle(AppColors.loss)
                    }
                }

                FormError(message: errorMessage)

                if didSave {
                    Label("Password updated", systemImage: "checkmark.circle.fill")
                        .font(.subheadline.weight(.semibold))
                        .foregroundStyle(AppColors.gain)
                        .transition(.opacity)
                }

                PrimaryButton(title: "Update password", isWorking: isSaving) {
                    Task {
                        await save()
                    }
                }
                .disabled(!canSave)
            }
            .padding(20)
        }
        .scrollDismissesKeyboard(.interactively)
        .screenBackground()
        .navigationTitle(session.user?.hasPassword == false ? "Set password" : "Change password")
        #if os(iOS)
        .navigationBarTitleDisplayMode(.inline)
        #endif
        .sensoryFeedback(.success, trigger: didSave)
    }

    private func save() async {
        isSaving = true
        errorMessage = nil

        defer { isSaving = false }

        do {
            try await session.changePassword(current: currentPassword, new: newPassword)

            withAnimation {
                didSave = true
            }

            // Leave the confirmation on screen briefly before going back.
            try? await Task.sleep(for: .seconds(1))
            dismiss()
        } catch {
            errorMessage = error.localizedDescription
        }
    }
}
