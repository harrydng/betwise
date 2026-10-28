import SwiftUI

struct SignUpView: View {
    let session: SessionModel

    @Environment(\.dismiss) private var dismiss

    @State private var name = ""
    @State private var email = ""
    @State private var password = ""
    @State private var experience: User.ExperienceLevel = .beginner
    @State private var goal: User.InvestmentGoal = .longTermGrowth
    @State private var risk: User.RiskTolerance = .balanced
    @State private var isWorking = false
    @State private var errorMessage: String?

    private static let minimumPasswordLength = 8

    var body: some View {
        let trimmedName = name.trimmingCharacters(in: .whitespaces)
        let isPasswordLongEnough = password.count >= Self.minimumPasswordLength
        let canSubmit = !trimmedName.isEmpty && email.contains("@") && isPasswordLongEnough && !isWorking

        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 20) {
                    VStack(alignment: .leading, spacing: 8) {
                        Text("Create your account")
                            .font(.largeTitle.weight(.bold))
                            .foregroundStyle(AppColors.textPrimary)

                        Text("Practice investing without risking real money.")
                            .font(.subheadline)
                            .foregroundStyle(AppColors.textSecondary)
                    }

                    FormField(title: "Name", text: $name, kind: .name, prompt: "Your name")
                    FormField(title: "Email", text: $email, kind: .email, prompt: "you@example.com")

                    VStack(alignment: .leading, spacing: 6) {
                        FormField(title: "Password", text: $password, kind: .newPassword)

                        Text("At least \(Self.minimumPasswordLength) characters")
                            .font(.caption)
                            .foregroundStyle(
                                password.isEmpty || isPasswordLongEnough
                                    ? AppColors.textTertiary
                                    : AppColors.loss
                            )
                    }

                    InvestorProfilePickers(experience: $experience, goal: $goal, risk: $risk)

                    FormError(message: errorMessage)

                    PrimaryButton(title: "Create account", isWorking: isWorking) {
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
    }

    private func submit() async {
        isWorking = true
        errorMessage = nil

        defer { isWorking = false }

        do {
            try await session.signUp(
                CreateUserRequest(
                    name: name.trimmingCharacters(in: .whitespaces),
                    email: email.trimmingCharacters(in: .whitespaces),
                    password: password,
                    experienceLevel: experience,
                    investmentGoal: goal,
                    riskTolerance: risk
                )
            )
        } catch {
            withAnimation {
                errorMessage = error.localizedDescription
            }
        }
    }
}

/// The three investor-profile questions, shared by sign-up and profile
/// editing.
struct InvestorProfilePickers: View {
    @Binding var experience: User.ExperienceLevel
    @Binding var goal: User.InvestmentGoal
    @Binding var risk: User.RiskTolerance

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("About you")
                .font(.headline)
                .foregroundStyle(AppColors.textPrimary)

            VStack(spacing: 0) {
                PickerRow(title: "Experience") {
                    Picker("Experience", selection: $experience) {
                        ForEach(User.ExperienceLevel.allCases, id: \.self) { level in
                            Text(level.displayName).tag(level)
                        }
                    }
                }

                Divider().overlay(AppColors.stroke)

                PickerRow(title: "Goal") {
                    Picker("Goal", selection: $goal) {
                        ForEach(User.InvestmentGoal.allCases, id: \.self) { goal in
                            Text(goal.displayName).tag(goal)
                        }
                    }
                }

                Divider().overlay(AppColors.stroke)

                PickerRow(title: "Risk tolerance") {
                    Picker("Risk tolerance", selection: $risk) {
                        ForEach(User.RiskTolerance.allCases, id: \.self) { risk in
                            Text(risk.displayName).tag(risk)
                        }
                    }
                }
            }
            .card(padding: 4, cornerRadius: 18)
        }
    }
}

private struct PickerRow<Content: View>: View {
    let title: LocalizedStringKey
    @ViewBuilder let content: Content

    var body: some View {
        HStack {
            Text(title)
                .foregroundStyle(AppColors.textPrimary)

            Spacer(minLength: 8)

            content
                .labelsHidden()
                .pickerStyle(.menu)
                .tint(AppColors.accentBright)
                .fixedSize()
        }
        .padding(.leading, 14)
        .padding(.vertical, 6)
        .frame(minHeight: 48)
    }
}

#Preview {
    SignUpView(session: SessionModel())
        .preferredColorScheme(.dark)
}
