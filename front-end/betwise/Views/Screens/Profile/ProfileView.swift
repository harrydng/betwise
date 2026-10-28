import SwiftUI

struct ProfileView: View {
    let session: SessionModel

    @Environment(\.dismiss) private var dismiss

    @State private var isConfirmingDelete = false
    @State private var isDeleting = false
    @State private var errorMessage: String?

    var body: some View {
        NavigationStack {
            List {
                Section {
                    ProfileHeader(
                        name: session.user?.name,
                        email: session.user?.email,
                        memberSince: session.user?.createdAt,
                        isGuest: session.state == .guest
                    )
                }
                .listRowBackground(Color.clear)
                .listRowInsets(EdgeInsets())

                if session.state == .guest {
                    GuestSection {
                        session.signOut()
                    }
                } else if let user = session.user {
                    InvestorProfileSection(
                        experience: user.experienceLevel,
                        goal: user.investmentGoal,
                        risk: user.riskTolerance,
                        dateOfBirth: user.dateOfBirth
                    )

                    Section {
                        NavigationLink {
                            EditProfileView(session: session)
                        } label: {
                            Label("Edit profile", systemImage: "pencil")
                        }

                        NavigationLink {
                            ChangePasswordView(session: session)
                        } label: {
                            Label("Change password", systemImage: "key")
                        }
                    }
                    .listRowBackground(AppColors.surface)

                    Section {
                        Button {
                            session.signOut()
                        } label: {
                            Label("Sign out", systemImage: "rectangle.portrait.and.arrow.right")
                        }

                        Button(role: .destructive) {
                            isConfirmingDelete = true
                        } label: {
                            if isDeleting {
                                ProgressView()
                            } else {
                                Label("Delete account", systemImage: "trash")
                            }
                        }
                        .disabled(isDeleting)
                    } footer: {
                        if let errorMessage {
                            Text(errorMessage)
                                .foregroundStyle(AppColors.loss)
                        }
                    }
                    .listRowBackground(AppColors.surface)
                } else {
                    ProgressView()
                        .frame(maxWidth: .infinity)
                        .listRowBackground(Color.clear)
                }
            }
            .scrollContentBackground(.hidden)
            .screenBackground()
            .navigationTitle("Profile")
            #if os(iOS)
            .navigationBarTitleDisplayMode(.inline)
            #endif
            .toolbar {
                ToolbarItem(placement: .confirmationAction) {
                    Button("Done") {
                        dismiss()
                    }
                }
            }
            .confirmationDialog(
                "Delete your account?",
                isPresented: $isConfirmingDelete,
                titleVisibility: .visible
            ) {
                Button("Delete account", role: .destructive) {
                    Task {
                        await deleteAccount()
                    }
                }
            } message: {
                Text("Your portfolio, watchlists, and lesson progress will be permanently deleted. This can't be undone.")
            }
            .task {
                await session.loadUser()
            }
        }
        .presentationBackground(AppColors.background)
    }

    private func deleteAccount() async {
        isDeleting = true
        errorMessage = nil

        defer { isDeleting = false }

        do {
            try await session.deleteAccount()
        } catch {
            errorMessage = error.localizedDescription
        }
    }
}

// MARK: - Sections

private struct ProfileHeader: View {
    let name: String?
    let email: String?
    let memberSince: Date?
    let isGuest: Bool

    var body: some View {
        VStack(spacing: 12) {
            Text(initials)
                .font(.system(.title, design: .rounded, weight: .bold))
                .foregroundStyle(.white)
                .frame(width: 84, height: 84)
                .background(
                    LinearGradient(
                        colors: [AppColors.accentBright, AppColors.accent, AppColors.accentDeep],
                        startPoint: .topLeading,
                        endPoint: .bottomTrailing
                    ),
                    in: Circle()
                )
                .accessibilityHidden(true)

            VStack(spacing: 4) {
                Text(isGuest ? String(localized: "Guest") : (name ?? " "))
                    .font(.title2.weight(.bold))
                    .foregroundStyle(AppColors.textPrimary)

                if let email {
                    Text(email)
                        .font(.subheadline)
                        .foregroundStyle(AppColors.textSecondary)
                }

                if let memberSince {
                    Text("Member since \(memberSince, format: .dateTime.month(.wide).year())")
                        .font(.caption)
                        .foregroundStyle(AppColors.textTertiary)
                        .padding(.top, 2)
                }
            }
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 12)
        .accessibilityElement(children: .combine)
    }

    private var initials: String {
        guard !isGuest, let name else { return "?" }

        let letters = name
            .split(separator: " ")
            .prefix(2)
            .compactMap(\.first)

        return letters.isEmpty ? "?" : String(letters).uppercased()
    }
}

private struct GuestSection: View {
    let onSignIn: () -> Void

    var body: some View {
        Section {
            VStack(alignment: .leading, spacing: 12) {
                Text("You're exploring Betwise with sample data. Create an account to build your own portfolio, earn rewards, and get notifications.")
                    .font(.subheadline)
                    .foregroundStyle(AppColors.textSecondary)
                    .fixedSize(horizontal: false, vertical: true)

                PrimaryButton(title: "Sign in or create account") {
                    onSignIn()
                }
            }
            .padding(.vertical, 8)
        }
        .listRowBackground(AppColors.surface)
    }
}

private struct InvestorProfileSection: View {
    let experience: User.ExperienceLevel?
    let goal: User.InvestmentGoal?
    let risk: User.RiskTolerance?
    let dateOfBirth: Date?

    var body: some View {
        Section("Investor profile") {
            LabeledContent("Experience", value: experience?.displayName ?? String(localized: "Not set"))
            LabeledContent("Goal", value: goal?.displayName ?? String(localized: "Not set"))
            LabeledContent("Risk tolerance", value: risk?.displayName ?? String(localized: "Not set"))

            LabeledContent("Birthday") {
                if let dateOfBirth {
                    Text(dateOfBirth, format: .dateTime.month(.wide).day().year())
                } else {
                    Text("Not set")
                }
            }
        }
        .listRowBackground(AppColors.surface)
    }
}

#Preview("Guest") {
    let session = SessionModel()
    session.continueAsGuest()

    return ProfileView(session: session)
        .preferredColorScheme(.dark)
}
