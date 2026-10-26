import SwiftUI

struct LessonsView: View {
    private let lessons = [
        ("Stocks 101", "Beginner", 0.85, "chart.line.uptrend.xyaxis"),
//        ("Risk & Return", "Beginner", 0.45, "exclamationmark.triangle.fill"),
//        ("Diversification", "Intermediate", 0.20, "square.grid.2x2.fill"),
//        ("Fundamental Analysis", "Intermediate", 0.0, "doc.text.magnifyingglass")
    ]

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 18) {
                Text("Lessons")
                    .font(.system(size: 34, weight: .bold))
                    .foregroundStyle(AppColors.textPrimary)

                Text("Learn, practice, then put it to work.")
                    .foregroundStyle(AppColors.textSecondary)

                ForEach(Array(lessons.enumerated()), id: \.offset) { _, lesson in
                    HStack(spacing: 16) {
                        Image(systemName: lesson.3)
                            .font(.system(size: 20, weight: .semibold))
                            .foregroundStyle(AppColors.primaryBlue)
                            .frame(width: 48, height: 48)
                            .background(AppColors.surfaceLight)
                            .clipShape(Circle())

                        VStack(alignment: .leading, spacing: 7) {
                            Text(lesson.0)
                                .font(.headline)
                                .foregroundStyle(AppColors.textPrimary)

                            Text(lesson.1)
                                .font(.caption)
                                .foregroundStyle(AppColors.textSecondary)

                            ProgressView(value: lesson.2)
                                .tint(AppColors.primaryBlue)
                        }
                    }
                    .padding(17)
                    .background(AppColors.surface)
                    .clipShape(RoundedRectangle(cornerRadius: 22, style: .continuous))
                }
            }
            .padding(.horizontal, 20)
            .padding(.top, 26)
            .padding(.bottom, 120)
        }
    }
}

#Preview {
    ContentView()
}
