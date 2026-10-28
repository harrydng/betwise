import SwiftUI

struct LessonDetailView: View {
    let lessonID: Int
    let model: LessonsModel
    let portfolio: PortfolioModel

    @State private var isStarting = false
    @State private var isReading = false
    @State private var alertMessage: String?
    @State private var quizzes: [Quiz] = []
    @State private var selectedQuiz: Quiz?

    var body: some View {
        if let lesson = model.rowsByID[lessonID] {
            ScrollView {
                VStack(alignment: .leading, spacing: 24) {
                    LessonHeader(
                        title: lesson.title,
                        category: lesson.category,
                        topic: lesson.topic,
                        difficulty: lesson.difficulty
                    )

                    if let summary = lesson.summary {
                        Text(summary)
                            .font(.body)
                            .foregroundStyle(AppColors.textSecondary)
                            .fixedSize(horizontal: false, vertical: true)
                    }

                    if lesson.checkpointCount > 0 {
                        LessonProgressCard(
                            completed: lesson.completedCheckpoints,
                            total: lesson.checkpointCount
                        )
                    }

                    RewardCard(reward: lesson.reward, status: lesson.status)

                    if !quizzes.isEmpty {
                        QuizzesSection(quizzes: quizzes) { quiz in
                            selectedQuiz = quiz
                        }
                    }
                }
                .padding(.horizontal, 20)
                .padding(.bottom, 24)
            }
            .scrollIndicators(.hidden)
            .screenBackground()
            #if os(iOS)
            .navigationBarTitleDisplayMode(.inline)
            #endif
            .safeAreaInset(edge: .bottom) {
                LessonActionBar(
                    status: lesson.status,
                    checkpointCount: lesson.checkpointCount,
                    isWorking: isStarting
                ) {
                    Task {
                        await openReader(status: lesson.status)
                    }
                }
            }
            .navigationDestination(isPresented: $isReading) {
                LessonReaderView(
                    lessonID: lessonID,
                    reward: lesson.reward,
                    isReview: lesson.status == .completed,
                    model: model,
                    portfolio: portfolio
                )
            }
            .navigationDestination(item: $selectedQuiz) { quiz in
                QuizView(quiz: quiz)
            }
            .alert(
                "Can't open lesson",
                isPresented: Binding(
                    get: { alertMessage != nil },
                    set: { if !$0 { alertMessage = nil } }
                )
            ) {
                Button("OK", role: .cancel) {}
            } message: {
                Text(alertMessage ?? "")
            }
            // Reload when progress changes, since that unlocks quizzes.
            .task(id: lesson.completedCheckpoints) {
                await loadQuizzes()
            }
        } else {
            ContentUnavailableView("Lesson unavailable", systemImage: "book.closed")
                .screenBackground()
        }
    }

    private func openReader(status: LessonStatus) async {
        guard !model.usesSampleData else {
            alertMessage = String(localized: "Sign in to read lessons, track your progress, and earn rewards.")
            return
        }

        if status == .notStarted {
            isStarting = true
            defer { isStarting = false }

            do {
                try await model.start(lessonID: lessonID)
            } catch {
                alertMessage = error.localizedDescription
                return
            }
        }

        isReading = true
    }

    private func loadQuizzes() async {
        guard !model.usesSampleData else { return }

        if let loaded = try? await model.quizzes(for: lessonID) {
            quizzes = loaded
        }
    }
}

// MARK: - Sections

private struct LessonHeader: View {
    let title: String
    let category: String
    let topic: Lesson.Topic
    let difficulty: Lesson.Difficulty

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            TopicIcon(topic: topic, size: 64)

            VStack(alignment: .leading, spacing: 8) {
                Text(category.uppercased())
                    .font(.caption.weight(.bold))
                    .tracking(1)
                    .foregroundStyle(AppColors.accentBright)

                Text(title)
                    .font(.largeTitle.weight(.bold))
                    .foregroundStyle(AppColors.textPrimary)
                    .fixedSize(horizontal: false, vertical: true)

                DifficultyMeter(difficulty: difficulty)
            }
        }
        .padding(.top, 8)
        .accessibilityElement(children: .combine)
    }
}

private struct RewardCard: View {
    let reward: Decimal
    let status: LessonStatus

    var body: some View {
        HStack(spacing: 16) {
            Image(systemName: status == .completed ? "checkmark.seal.fill" : "gift.fill")
                .font(.title2)
                .foregroundStyle(AppColors.accentBright)
                .frame(width: 48, height: 48)
                .background(AppColors.accent.opacity(0.16), in: Circle())
                .accessibilityHidden(true)

            VStack(alignment: .leading, spacing: 4) {
                Text(reward, format: .currency(code: "USD").precision(.fractionLength(0)))
                    .font(.title3.weight(.bold))
                    .foregroundStyle(AppColors.textPrimary)

                Text(status == .completed
                     ? "Added to your buying power"
                     : "Added to your buying power when you finish")
                    .font(.subheadline)
                    .foregroundStyle(AppColors.textSecondary)
            }
        }
        .card()
        .accessibilityElement(children: .combine)
    }
}

private struct LessonProgressCard: View {
    let completed: Int
    let total: Int

    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            HStack {
                Text("\(total) sections")
                    .font(.headline)
                    .foregroundStyle(AppColors.textPrimary)

                Spacer()

                Text("\(min(completed, total)) of \(total) done")
                    .font(.subheadline)
                    .monospacedDigit()
                    .foregroundStyle(AppColors.textSecondary)
            }

            ProgressView(value: Double(min(completed, total)), total: Double(max(total, 1)))
                .tint(AppColors.accent)
        }
        .card()
        .accessibilityElement(children: .combine)
    }
}

private struct QuizzesSection: View {
    let quizzes: [Quiz]
    let onOpen: (Quiz) -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 14) {
            SectionHeader(title: "Quizzes")

            VStack(spacing: 0) {
                ForEach(quizzes) { quiz in
                    Button {
                        onOpen(quiz)
                    } label: {
                        QuizRow(
                            title: quiz.title,
                            questionCount: quiz.questions.count,
                            isLocked: quiz.locked
                        )
                    }
                    .buttonStyle(.plain)
                    .disabled(quiz.locked)

                    if quiz.id != quizzes.last?.id {
                        Divider().overlay(AppColors.stroke)
                    }
                }
            }
            .card(padding: 16)
        }
    }
}

private struct QuizRow: View {
    let title: String
    let questionCount: Int
    let isLocked: Bool

    var body: some View {
        HStack(spacing: 14) {
            Image(systemName: isLocked ? "lock.fill" : "questionmark.circle.fill")
                .font(.title3)
                .foregroundStyle(isLocked ? AppColors.textTertiary : AppColors.accentBright)
                .frame(width: 40, height: 40)
                .background(AppColors.accent.opacity(isLocked ? 0.06 : 0.16), in: Circle())
                .accessibilityHidden(true)

            VStack(alignment: .leading, spacing: 3) {
                Text(title)
                    .font(.headline)
                    .foregroundStyle(isLocked ? AppColors.textSecondary : AppColors.textPrimary)

                Text(isLocked ? "Keep reading to unlock" : "\(questionCount) questions")
                    .font(.subheadline)
                    .foregroundStyle(AppColors.textSecondary)
            }

            Spacer(minLength: 8)

            if !isLocked {
                Image(systemName: "chevron.right")
                    .font(.footnote.weight(.semibold))
                    .foregroundStyle(AppColors.textTertiary)
                    .accessibilityHidden(true)
            }
        }
        .padding(.vertical, 10)
        .contentShape(.rect)
        .accessibilityElement(children: .combine)
    }
}

private struct LessonActionBar: View {
    let status: LessonStatus
    let checkpointCount: Int
    let isWorking: Bool
    let action: () -> Void

    var body: some View {
        Button {
            action()
        } label: {
            Group {
                if isWorking {
                    ProgressView()
                } else {
                    switch status {
                    case .notStarted:
                        Text("Start lesson")
                    case .inProgress(let checkpoint):
                        Text("Continue: section \(min(checkpoint, max(checkpointCount, 1))) of \(max(checkpointCount, 1))")
                    case .completed:
                        Label("Review lesson", systemImage: "arrow.counterclockwise")
                    }
                }
            }
            .font(.headline)
            .frame(maxWidth: .infinity)
        }
        .buttonStyle(.glassProminent)
        .tint(AppColors.accent)
        .controlSize(.large)
        .buttonBorderShape(.capsule)
        .disabled(isWorking)
        .padding(.horizontal, 20)
        .padding(.vertical, 10)
    }
}

#Preview {
    NavigationStack {
        LessonDetailView(
            lessonID: 4,
            model: LessonsModel(usesSampleData: true),
            portfolio: PortfolioModel(usesSampleData: true)
        )
    }
    .preferredColorScheme(.dark)
}
