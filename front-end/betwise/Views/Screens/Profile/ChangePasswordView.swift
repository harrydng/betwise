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
        let isLongEnough = newPassword.count >= Self.minimumPasswordLength
        let matches = !confirmation.isEmpty && confirmation == newPassword
        let canSave = !currentPassword.isEmpty && isLongEnough && matches && !isSaving

        ScrollView {
            VStack(alignment: .leading, spacing: 20) {
                FormField(title: "Current password", text: $currentPassword, kind: .password)

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
        .navigationTitle("Change password")
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
