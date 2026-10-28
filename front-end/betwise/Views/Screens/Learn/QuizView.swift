import SwiftUI

/// Takes a quiz one question at a time. Answers are final once checked,
/// matching the backend, and a quiz left halfway resumes where it stopped.
struct QuizView: View {
    let quiz: Quiz

    @Environment(\.dismiss) private var dismiss

    @State private var index = 0
    @State private var selectedAnswer: String?
    @State private var lastResult: QuizAnswerResult?
    @State private var finalScore: Int?
    @State private var hasStarted = false
    @State private var isWorking = false
    @State private var errorMessage: String?

    private var questions: [QuizQuestion] {
        quiz.questions.sorted { $0.questionOrder < $1.questionOrder }
    }

    var body: some View {
        let questions = questions

        Group {
            if let finalScore {
                QuizResultView(score: finalScore, title: quiz.title) {
                    dismiss()
                }
            } else if !hasStarted {
                VStack(spacing: 16) {
                    ProgressView()
                    FormError(message: errorMessage)
                }
                .padding(20)
                .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else if index < questions.count {
                let question = questions[index]

                ScrollView {
                    QuestionCard(
                        number: index + 1,
                        total: questions.count,
                        question: question.question,
                        choices: question.choiceList,
                        selectedAnswer: $selectedAnswer,
                        result: lastResult
                    )
                    .padding(20)
                }
                .scrollIndicators(.hidden)
                .id(question.id)
                .safeAreaInset(edge: .bottom) {
                    VStack(spacing: 10) {
                        FormError(message: errorMessage)

                        PrimaryButton(title: buttonTitle(isLast: index == questions.count - 1), isWorking: isWorking) {
                            Task {
                                await advance(question: question, isLast: index == questions.count - 1)
                            }
                        }
                        .disabled(selectedAnswer == nil || isWorking)
                    }
                    .padding(.horizontal, 20)
                    .padding(.vertical, 10)
                }
            } else {
                // Every question was answered in an earlier session.
                VStack(spacing: 16) {
                    Text("You've answered every question.")
                        .foregroundStyle(AppColors.textSecondary)

                    FormError(message: errorMessage)

                    PrimaryButton(title: "See results", isWorking: isWorking) {
                        Task {
                            await complete()
                        }
                    }
                }
                .padding(20)
                .frame(maxWidth: .infinity, maxHeight: .infinity)
            }
        }
        .screenBackground()
        .navigationTitle(quiz.title)
        #if os(iOS)
        .navigationBarTitleDisplayMode(.inline)
        #endif
        .sensoryFeedback(trigger: lastResult) { _, result in
            guard let result else { return nil }
            return result.correct ? .success : .error
        }
        .task {
            await start()
        }
    }

    private func buttonTitle(isLast: Bool) -> LocalizedStringKey {
        if lastResult == nil {
            return "Check answer"
        }

        return isLast ? "See results" : "Next question"
    }

    private func start() async {
        do {
            let progress = try await QuizService.shared.startQuiz(id: quiz.id)

            if progress.completed {
                finalScore = progress.score
            } else {
                // `currentQuestion` starts at 1 and goes up with each answer.
                index = max(progress.currentQuestion - 1, 0)
            }

            hasStarted = true
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    private func advance(question: QuizQuestion, isLast: Bool) async {
        errorMessage = nil

        // Second tap: move on from a checked answer.
        if lastResult != nil {
            if isLast {
                await complete()
            } else {
                withAnimation(.snappy) {
                    index += 1
                    selectedAnswer = nil
                    lastResult = nil
                }
            }
            return
        }

        guard let selectedAnswer else { return }

        isWorking = true
        defer { isWorking = false }

        do {
            let result = try await QuizService.shared.submitAnswer(
                quizId: quiz.id,
                questionId: question.id,
                selectedAnswer: selectedAnswer
            )

            withAnimation(.snappy) {
                lastResult = result
            }
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    private func complete() async {
        isWorking = true
        defer { isWorking = false }

        do {
            let progress = try await QuizService.shared.completeQuiz(id: quiz.id)

            withAnimation(.snappy) {
                finalScore = progress.score
            }
        } catch {
            errorMessage = error.localizedDescription
        }
    }
}

// MARK: - Pieces

private struct QuestionCard: View {
    let number: Int
    let total: Int
    let question: String
    let choices: [String]
    @Binding var selectedAnswer: String?
    let result: QuizAnswerResult?

    var body: some View {
        VStack(alignment: .leading, spacing: 20) {
            VStack(alignment: .leading, spacing: 8) {
                Text("Question \(number) of \(total)")
                    .font(.caption.weight(.semibold))
                    .foregroundStyle(AppColors.textSecondary)

                ProgressView(value: Double(number), total: Double(max(total, 1)))
                    .tint(AppColors.accent)
            }

            Text(question)
                .font(.title2.weight(.bold))
                .foregroundStyle(AppColors.textPrimary)
                .fixedSize(horizontal: false, vertical: true)

            VStack(spacing: 10) {
                ForEach(choices, id: \.self) { choice in
                    ChoiceButton(
                        text: choice,
                        isSelected: choice == selectedAnswer,
                        result: choice == selectedAnswer ? result : nil,
                        isLocked: result != nil
                    ) {
                        selectedAnswer = choice
                    }
                }
            }

            if let result {
                Label(
                    result.correct ? "Correct!" : "Not quite. Keep going, you'll get the next one.",
                    systemImage: result.correct ? "checkmark.circle.fill" : "xmark.circle.fill"
                )
                .font(.subheadline.weight(.semibold))
                .foregroundStyle(result.correct ? AppColors.gain : AppColors.loss)
                .transition(.opacity)
            }
        }
    }
}

private struct ChoiceButton: View {
    let text: String
    let isSelected: Bool
    let result: QuizAnswerResult?
    let isLocked: Bool
    let action: () -> Void

    var body: some View {
        let tint: Color = switch result?.correct {
        case true: AppColors.gain
        case false: AppColors.loss
        case nil: AppColors.accent
        }

        Button {
            action()
        } label: {
            HStack(spacing: 12) {
                Text(text)
                    .font(.body.weight(isSelected ? .semibold : .regular))
                    .foregroundStyle(AppColors.textPrimary)
                    .multilineTextAlignment(.leading)

                Spacer(minLength: 8)

                Image(systemName: isSelected ? "largecircle.fill.circle" : "circle")
                    .foregroundStyle(isSelected ? tint : AppColors.textTertiary)
                    .accessibilityHidden(true)
            }
            .padding(16)
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(
                isSelected ? tint.opacity(0.14) : AppColors.surface,
                in: .rect(cornerRadius: 16, style: .continuous)
            )
            .overlay {
                RoundedRectangle(cornerRadius: 16, style: .continuous)
                    .strokeBorder(isSelected ? tint : AppColors.stroke, lineWidth: isSelected ? 1.5 : 1)
            }
            .contentShape(.rect)
        }
        .buttonStyle(.plain)
        .disabled(isLocked)
        .accessibilityAddTraits(isSelected ? .isSelected : [])
    }
}

private struct QuizResultView: View {
    let score: Int
    let title: String
    let onDone: () -> Void

    var body: some View {
        let fraction = Double(score) / 100

        VStack(spacing: 24) {
            Spacer()

            ZStack {
                Circle()
                    .stroke(AppColors.surfaceRaised, lineWidth: 12)

                Circle()
                    .trim(from: 0, to: fraction)
                    .stroke(AppColors.accent, style: StrokeStyle(lineWidth: 12, lineCap: .round))
                    .rotationEffect(.degrees(-90))

                Text(fraction, format: .percent.precision(.fractionLength(0)))
                    .font(.system(.largeTitle, design: .rounded, weight: .bold))
                    .monospacedDigit()
                    .foregroundStyle(AppColors.textPrimary)
            }
            .frame(width: 160, height: 160)
            .accessibilityElement(children: .ignore)
            .accessibilityLabel("Score \(score) percent")

            VStack(spacing: 8) {
                Text(score >= 80 ? "Great work!" : score >= 50 ? "Nice effort" : "Keep practicing")
                    .font(.title2.weight(.bold))
                    .foregroundStyle(AppColors.textPrimary)

                Text("You finished \(title).")
                    .font(.subheadline)
                    .foregroundStyle(AppColors.textSecondary)
            }

            Spacer()

            PrimaryButton(title: "Done") {
                onDone()
            }
        }
        .padding(20)
    }
}
