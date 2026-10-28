import SwiftUI

struct LessonsView: View {
    @Bindable var model: LessonsModel
    let portfolio: PortfolioModel

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 28) {
                if !model.hasLoaded {
                    ProgressView()
                        .frame(maxWidth: .infinity, minHeight: 300)
                } else if model.rows.isEmpty {
                    ContentUnavailableView(
                        "No lessons yet",
                        systemImage: "graduationcap",
                        description: Text("Lessons will appear here once they're added to Betwise.")
                    )
                    .card()
                } else {
                    LearnProgressCard(
                        completed: model.completedCount,
                        total: model.rows.count,
                        earned: model.earnedRewards,
                        remaining: model.remainingRewards
                    )

                    if let lesson = model.continueLesson {
                        ContinueLessonCard(lesson: lesson)
                    }

                    VStack(alignment: .leading, spacing: 14) {
                        SectionHeader(title: "Lessons", detail: model.visibleRows.count.formatted())

                        FilterChips(options: model.categoryOptions, selection: $model.selectedCategory)

                        LessonList(rows: model.visibleRows)
                    }
                }
            }
            .padding(.horizontal, 20)
            .padding(.bottom, 32)
        }
        .scrollIndicators(.hidden)
        .screenBackground()
        .navigationTitle("Learn")
        .navigationDestination(for: LessonRowItem.self) { lesson in
            LessonDetailView(lessonID: lesson.id, model: model, portfolio: portfolio)
        }
        .refreshable {
            await model.refresh()
        }
        .task {
            await model.refresh()
        }
    }
}

// MARK: - Sections

private struct LearnProgressCard: View {
    let completed: Int
    let total: Int
    let earned: Decimal
    let remaining: Decimal

    var body: some View {
        let fraction = total > 0 ? Double(completed) / Double(total) : 0

        HStack(spacing: 20) {
            ZStack {
                Circle()
                    .stroke(.white.opacity(0.18), lineWidth: 8)

                Circle()
                    .trim(from: 0, to: fraction)
                    .stroke(.white, style: StrokeStyle(lineWidth: 8, lineCap: .round))
                    .rotationEffect(.degrees(-90))

                Text(fraction, format: .percent.precision(.fractionLength(0)))
                    .font(.subheadline.weight(.bold))
                    .monospacedDigit()
                    .foregroundStyle(.white)
            }
            .frame(width: 76, height: 76)
            .accessibilityHidden(true)

            VStack(alignment: .leading, spacing: 6) {
                Text("\(completed) of \(total) lessons complete")
                    .font(.headline)
                    .foregroundStyle(.white)

                Text("You've earned \(earned, format: .currency(code: "USD").precision(.fractionLength(0))) in buying power.")
                    .font(.subheadline)
                    .foregroundStyle(.white.opacity(0.85))

                if remaining > 0 {
                    Text("\(remaining, format: .currency(code: "USD").precision(.fractionLength(0))) more to earn")
                        .font(.caption.weight(.semibold))
                        .foregroundStyle(.white)
                        .padding(.horizontal, 10)
                        .padding(.vertical, 4)
                        .background(.white.opacity(0.18), in: Capsule())
                        .padding(.top, 2)
                }
            }
            .fixedSize(horizontal: false, vertical: true)

            Spacer(minLength: 0)
        }
        .padding(20)
        .background(
            LinearGradient(
                colors: [AppColors.accent, AppColors.accentDeep],
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            ),
            in: .rect(cornerRadius: 24, style: .continuous)
        )
        .accessibilityElement(children: .combine)
    }
}

private struct ContinueLessonCard: View {
    let lesson: LessonRowItem

    var body: some View {
        VStack(alignment: .leading, spacing: 14) {
            SectionHeader(title: "Continue learning")

            NavigationLink(value: lesson) {
                HStack(spacing: 14) {
                    TopicIcon(topic: lesson.topic, size: 52)

                    VStack(alignment: .leading, spacing: 4) {
                        Text(lesson.title)
                            .font(.headline)
                            .foregroundStyle(AppColors.textPrimary)

                        if case .inProgress(let checkpoint) = lesson.status {
                            Text("Checkpoint \(checkpoint) · \(lesson.category)")
                                .font(.subheadline)
                                .foregroundStyle(AppColors.textSecondary)
                        }
                    }

                    Spacer(minLength: 8)

                    Image(systemName: "play.fill")
                        .font(.subheadline)
                        .foregroundStyle(.white)
                        .frame(width: 40, height: 40)
                        .background(AppColors.accent, in: Circle())
                        .accessibilityHidden(true)
                }
                .card(padding: 16)
                .contentShape(.rect)
            }
            .buttonStyle(.plain)
        }
    }
}

private struct LessonList: View {
    let rows: [LessonRowItem]

    var body: some View {
        VStack(spacing: 0) {
            ForEach(rows) { row in
                NavigationLink(value: row) {
                    LessonRow(
                        title: row.title,
                        topic: row.topic,
                        difficulty: row.difficulty,
                        reward: row.reward,
                        status: row.status
                    )
                }
                .buttonStyle(.plain)

                if row.id != rows.last?.id {
                    Divider().overlay(AppColors.stroke)
                }
            }
        }
        .card(padding: 16)
    }
}

private struct LessonRow: View {
    let title: String
    let topic: Lesson.Topic
    let difficulty: Lesson.Difficulty
    let reward: Decimal
    let status: LessonStatus

    var body: some View {
        HStack(spacing: 14) {
            TopicIcon(topic: topic, size: 44)

            VStack(alignment: .leading, spacing: 5) {
                Text(title)
                    .font(.headline)
                    .foregroundStyle(AppColors.textPrimary)

                HStack(spacing: 8) {
                    DifficultyMeter(difficulty: difficulty)

                    Text("+\(reward, format: .currency(code: "USD").precision(.fractionLength(0)))")
                        .font(.caption.weight(.semibold))
                        .foregroundStyle(AppColors.accentBright)
                }
            }

            Spacer(minLength: 8)

            LessonStatusIcon(status: status)
        }
        .padding(.vertical, 12)
        .contentShape(.rect)
        .accessibilityElement(children: .combine)
    }
}

// MARK: - Shared pieces

struct TopicIcon: View {
    let topic: Lesson.Topic
    var size: CGFloat = 44

    var body: some View {
        Image(systemName: topic.symbolName)
            .font(.system(size: size * 0.42, weight: .semibold))
            .foregroundStyle(AppColors.accentBright)
            .frame(width: size, height: size)
            .background(AppColors.accent.opacity(0.16), in: .rect(cornerRadius: size * 0.3, style: .continuous))
            .accessibilityHidden(true)
    }
}

struct DifficultyMeter: View {
    let difficulty: Lesson.Difficulty

    var body: some View {
        HStack(spacing: 5) {
            HStack(spacing: 2) {
                ForEach(1...3, id: \.self) { level in
                    Capsule()
                        .fill(level <= difficulty.level ? AppColors.accentBright : AppColors.surfaceRaised)
                        .frame(width: 4, height: 6 + CGFloat(level) * 2)
                }
            }
            .accessibilityHidden(true)

            Text(difficulty.displayName)
                .font(.caption)
                .foregroundStyle(AppColors.textSecondary)
        }
    }
}

private struct LessonStatusIcon: View {
    let status: LessonStatus

    var body: some View {
        HStack {
            switch status {
            case .completed:
                Image(systemName: "checkmark.circle.fill")
                    .foregroundStyle(AppColors.accent)
                    .accessibilityLabel("Completed")
            case .inProgress:
                Image(systemName: "circle.lefthalf.filled")
                    .foregroundStyle(AppColors.accentBright)
                    .accessibilityLabel("In progress")
            case .notStarted:
                Image(systemName: "chevron.right")
                    .font(.footnote.weight(.semibold))
                    .foregroundStyle(AppColors.textTertiary)
                    .accessibilityLabel("Not started")
            }
        }
        .font(.title3)
    }
}

#Preview {
    NavigationStack {
        LessonsView(model: LessonsModel(usesSampleData: true), portfolio: PortfolioModel(usesSampleData: true))
    }
    .preferredColorScheme(.dark)
}
