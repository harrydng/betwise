import SwiftUI

struct SignInView: View {
    let session: SessionModel

    @Environment(\.dismiss) private var dismiss

    @State private var email = ""
    @State private var password = ""
    @State private var isWorking = false
    @State private var errorMessage: String?

    var body: some View {
        let canSubmit = email.contains("@") && !password.isEmpty && !isWorking

        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 20) {
                    Text("Welcome back")
                        .font(.largeTitle.weight(.bold))
                        .foregroundStyle(AppColors.textPrimary)

                    FormField(title: "Email", text: $email, kind: .email, prompt: "you@example.com")
                    FormField(title: "Password", text: $password, kind: .password)

                    FormError(message: errorMessage)

                    PrimaryButton(title: "Sign in", isWorking: isWorking) {
                        Task {
                            await submit()
                        }
                    }
                    .disabled(!canSubmit)
                    .padding(.top, 8)
                }
                .padding(24)
            }
            .scrollDismissesKeyboard(.interactively)
            .screenBackground()
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel", role: .cancel) {
                        dismiss()
                    }
                }
            }
        }
        .presentationBackground(AppColors.background)
        .onSubmit {
            guard canSubmit else { return }

            Task {
                await submit()
            }
        }
    }

    private func submit() async {
        isWorking = true
        errorMessage = nil

        defer { isWorking = false }

        do {
            try await session.signIn(
                email: email.trimmingCharacters(in: .whitespaces),
                password: password
            )
        } catch {
            withAnimation {
                errorMessage = error.localizedDescription
            }
        }
    }
}

#Preview {
    SignInView(session: SessionModel())
        .preferredColorScheme(.dark)
}
