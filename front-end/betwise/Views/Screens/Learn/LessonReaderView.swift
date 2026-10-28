import SwiftUI

/// Steps through a lesson's checkpoints one at a time, saving progress as
/// the reader moves forward, and pays the reward on the last one.
struct LessonReaderView: View {
    let lessonID: Int
    let reward: Decimal
    let isReview: Bool
    let model: LessonsModel
    let portfolio: PortfolioModel

    @Environment(\.dismiss) private var dismiss

    @State private var checkpoints: [LessonCheckpoint] = []
    @State private var index = 0
    @State private var hasLoaded = false
    @State private var loadError: String?
    @State private var isSaving = false
    @State private var actionError: String?
    @State private var didFinish = false

    var body: some View {
        Group {
            if didFinish {
                LessonCompleteView(reward: reward) {
                    dismiss()
                }
            } else if let loadError {
                ContentUnavailableView(
                    "Couldn't load this lesson",
                    systemImage: "wifi.exclamationmark",
                    description: Text(loadError)
                )
            } else if !hasLoaded {
                ProgressView()
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else if checkpoints.isEmpty {
                ContentUnavailableView(
                    "Nothing to read yet",
                    systemImage: "doc.text",
                    description: Text("This lesson doesn't have any content yet.")
                )
            } else {
                VStack(spacing: 0) {
                    ReaderProgressBar(current: index + 1, total: checkpoints.count)
                        .padding(.horizontal, 20)
                        .padding(.vertical, 12)

                    ScrollView {
                        CheckpointContent(
                            position: checkpoints[index].position,
                            title: checkpoints[index].title,
                            markdown: checkpoints[index].body
                        )
                        .padding(.horizontal, 20)
                        .padding(.bottom, 24)
                    }
                    .scrollIndicators(.hidden)
                    // A new scroll view per checkpoint, so each one starts at the top.
                    .id(index)
                    .transition(.asymmetric(
                        insertion: .move(edge: .trailing).combined(with: .opacity),
                        removal: .move(edge: .leading).combined(with: .opacity)
                    ))
                }
                .safeAreaInset(edge: .bottom) {
                    ReaderControls(
                        isFirst: index == 0,
                        isLast: index == checkpoints.count - 1,
                        isReview: isReview,
                        reward: reward,
                        isSaving: isSaving,
                        errorMessage: actionError,
                        onBack: goBack,
                        onNext: {
                            Task {
                                await goForward()
                            }
                        }
                    )
                }
            }
        }
        .screenBackground()
        .navigationTitle(didFinish ? "" : String(localized: "Lesson"))
        #if os(iOS)
        .navigationBarTitleDisplayMode(.inline)
        #endif
        .task {
            await load()
        }
    }

    private func load() async {
        do {
            checkpoints = try await model.checkpoints(for: lessonID)

            // Pick up where the reader left off.
            let current = model.progress(for: lessonID)?.currentCheckpoint ?? 1
            index = isReview ? 0 : min(max(current, 1), max(checkpoints.count, 1)) - 1
        } catch {
            loadError = error.localizedDescription
        }

        hasLoaded = true
    }

    private func goBack() {
        actionError = nil

        withAnimation(.snappy) {
            index = max(index - 1, 0)
        }
    }

    private func goForward() async {
        actionError = nil

        let finishedPosition = index + 1
        let isLast = index == checkpoints.count - 1

        // Reviewing a finished lesson just pages through it.
        guard !isReview else {
            if isLast {
                dismiss()
            } else {
                withAnimation(.snappy) { index += 1 }
            }
            return
        }

        isSaving = true
        defer { isSaving = false }

        do {
            try await model.saveProgress(
                lessonID: lessonID,
                currentCheckpoint: isLast ? finishedPosition : finishedPosition + 1,
                completedCheckpoints: finishedPosition
            )

            if isLast {
                try await finish()
            } else {
                withAnimation(.snappy) { index += 1 }
            }
        } catch {
            actionError = error.localizedDescription
        }
    }

    private func finish() async throws {
        guard let portfolioID = portfolio.portfolioID else {
            actionError = String(localized: "Create a portfolio on the Portfolio tab to receive your reward, then come back to finish.")
            return
        }

        try await model.complete(lessonID: lessonID, portfolioID: portfolioID)

        // The reward just landed in the portfolio's cash.
        await portfolio.refresh()

        withAnimation(.snappy) {
            didFinish = true
        }
    }
}

// MARK: - Pieces

private struct ReaderProgressBar: View {
    let current: Int
    let total: Int

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("Section \(current) of \(total)")
                .font(.caption.weight(.semibold))
                .foregroundStyle(AppColors.textSecondary)

            ProgressView(value: Double(current), total: Double(max(total, 1)))
                .tint(AppColors.accent)
                .animation(.snappy, value: current)
        }
        .accessibilityElement(children: .combine)
    }
}

private struct CheckpointContent: View {
    let position: Int
    let title: String
    let markdown: String

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text(title)
                .font(.title.weight(.bold))
                .foregroundStyle(AppColors.textPrimary)
                .fixedSize(horizontal: false, vertical: true)
                .accessibilityAddTraits(.isHeader)

            Text(Self.render(markdown))
                .font(.body)
                .lineSpacing(5)
                .foregroundStyle(AppColors.textPrimary.opacity(0.88))
                .tint(AppColors.accentBright)
                .fixedSize(horizontal: false, vertical: true)
                .textSelection(.enabled)
        }
        .padding(.top, 8)
        .frame(maxWidth: .infinity, alignment: .leading)
    }

    /// Inline Markdown (**bold**, *italic*, links) with line breaks kept,
    /// so "- " lines read as a list. Falls back to plain text.
    private static func render(_ markdown: String) -> AttributedString {
        let options = AttributedString.MarkdownParsingOptions(
            interpretedSyntax: .inlineOnlyPreservingWhitespace
        )

        return (try? AttributedString(markdown: markdown, options: options))
            ?? AttributedString(markdown)
    }
}

private struct ReaderControls: View {
    let isFirst: Bool
    let isLast: Bool
    let isReview: Bool
    let reward: Decimal
    let isSaving: Bool
    let errorMessage: String?
    let onBack: () -> Void
    let onNext: () -> Void

    var body: some View {
        VStack(spacing: 10) {
            FormError(message: errorMessage)

            HStack(spacing: 12) {
                if !isFirst {
                    Button {
                        onBack()
                    } label: {
                        Label("Back", systemImage: "chevron.left")
                            .labelStyle(.iconOnly)
                            .font(.headline)
                            .frame(width: 52)
                    }
                    .buttonStyle(.glass)
                    .accessibilityLabel("Previous section")
                }

                Button {
                    onNext()
                } label: {
                    Group {
                        if isSaving {
                            ProgressView()
                        } else if isLast && isReview {
                            Text("Done")
                        } else if isLast {
                            Text("Finish and earn \(reward, format: .currency(code: "USD").precision(.fractionLength(0)))")
                        } else {
                            Text("Next")
                        }
                    }
                    .font(.headline)
                    .frame(maxWidth: .infinity)
                }
                .buttonStyle(.glassProminent)
                .tint(AppColors.accent)
                .disabled(isSaving)
            }
            .controlSize(.large)
            .buttonBorderShape(.capsule)
        }
        .padding(.horizontal, 20)
        .padding(.vertical, 10)
    }
}

private struct LessonCompleteView: View {
    let reward: Decimal
    let onDone: () -> Void

    @State private var hasAppeared = false

    var body: some View {
        VStack(spacing: 20) {
            Spacer()

            Image(systemName: "checkmark.seal.fill")
                .font(.system(size: 76))
                .foregroundStyle(AppColors.accent)
                .symbolEffect(.bounce, value: hasAppeared)
                .accessibilityHidden(true)

            VStack(spacing: 8) {
                Text("Lesson complete")
                    .font(.title.weight(.bold))
                    .foregroundStyle(AppColors.textPrimary)

                Text("\(reward, format: .currency(code: "USD").precision(.fractionLength(0))) has been added to your buying power.")
                    .font(.subheadline)
                    .foregroundStyle(AppColors.textSecondary)
                    .multilineTextAlignment(.center)
            }

            Spacer()

            PrimaryButton(title: "Done") {
                onDone()
            }
        }
        .padding(20)
        .sensoryFeedback(.success, trigger: hasAppeared)
        .onAppear {
            hasAppeared = true
        }
    }
}
