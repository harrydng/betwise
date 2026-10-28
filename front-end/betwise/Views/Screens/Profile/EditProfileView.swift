import SwiftUI

struct EditProfileView: View {
    let session: SessionModel

    @Environment(\.dismiss) private var dismiss

    @State private var name: String
    @State private var experience: User.ExperienceLevel
    @State private var goal: User.InvestmentGoal
    @State private var risk: User.RiskTolerance
    @State private var includesBirthday: Bool
    @State private var birthday: Date
    @State private var isSaving = false
    @State private var errorMessage: String?

    /// The API keeps fields it doesn't receive, so a birthday can be set or
    /// changed but not removed once saved.
    private let hadBirthday: Bool

    init(session: SessionModel) {
        let user = session.user

        self.session = session
        self.name = user?.name ?? ""
        self.experience = user?.experienceLevel ?? .beginner
        self.goal = user?.investmentGoal ?? .longTermGrowth
        self.risk = user?.riskTolerance ?? .balanced
        self.includesBirthday = user?.dateOfBirth != nil
        self.birthday = user?.dateOfBirth
            ?? Calendar.current.date(byAdding: .year, value: -25, to: .now)
            ?? .now
        self.hadBirthday = user?.dateOfBirth != nil
    }

    var body: some View {
        let trimmedName = name.trimmingCharacters(in: .whitespaces)

        ScrollView {
            VStack(alignment: .leading, spacing: 24) {
                FormField(title: "Name", text: $name, kind: .name)

                InvestorProfilePickers(experience: $experience, goal: $goal, risk: $risk)

                VStack(alignment: .leading, spacing: 12) {
                    Text("Birthday")
                        .font(.headline)
                        .foregroundStyle(AppColors.textPrimary)

                    VStack(spacing: 0) {
                        if !hadBirthday {
                            Toggle("Add birthday", isOn: $includesBirthday.animation())
                                .tint(AppColors.accent)
                                .padding(14)
                        }

                        if includesBirthday {
                            if !hadBirthday {
                                Divider().overlay(AppColors.stroke)
                            }

                            DatePicker(
                                "Date of birth",
                                selection: $birthday,
                                in: ...Date.now,
                                displayedComponents: .date
                            )
                            .tint(AppColors.accentBright)
                            .padding(14)
                        }
                    }
                    .card(padding: 0, cornerRadius: 18)
                }

                FormError(message: errorMessage)

                PrimaryButton(title: "Save changes", isWorking: isSaving) {
                    Task {
                        await save(name: trimmedName)
                    }
                }
                .disabled(trimmedName.isEmpty || isSaving)
            }
            .padding(20)
        }
        .scrollDismissesKeyboard(.interactively)
        .screenBackground()
        .navigationTitle("Edit profile")
        #if os(iOS)
        .navigationBarTitleDisplayMode(.inline)
        #endif
    }

    private func save(name: String) async {
        isSaving = true
        errorMessage = nil

        defer { isSaving = false }

        do {
            try await session.updateProfile(
                UpdateUserRequest(
                    name: name,
                    dateOfBirth: includesBirthday ? birthday : nil,
                    experienceLevel: experience,
                    investmentGoal: goal,
                    riskTolerance: risk
                )
            )

            dismiss()
        } catch {
            errorMessage = error.localizedDescription
        }
    }
}
