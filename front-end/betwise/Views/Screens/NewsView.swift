import SwiftUI

struct NewsView: View {
    private let stories = [
        ("Markets", "Tech stocks lead the market higher", "12 min ago"),
        ("Economy", "Investors watch inflation data this week", "34 min ago"),
        ("AAPL", "Apple shares move after product update", "1 hr ago"),
        ("NVDA", "Chip stocks remain in focus", "2 hrs ago")
    ]

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 18) {
                Text("News")
                    .font(.system(size: 34, weight: .bold))
                    .foregroundStyle(AppColors.textPrimary)

                Text("What is moving the market")
                    .foregroundStyle(AppColors.textSecondary)

                ForEach(Array(stories.enumerated()), id: \.offset) { _, story in
                    VStack(alignment: .leading, spacing: 10) {
                        HStack {
                            Text(story.0.uppercased())
                                .font(.caption2.bold())
                                .foregroundStyle(AppColors.primaryBlue)

                            Spacer()

                            Text(story.2)
                                .font(.caption)
                                .foregroundStyle(AppColors.textSecondary)
                        }

                        Text(story.1)
                            .font(.system(size: 18, weight: .semibold))
                            .foregroundStyle(AppColors.textPrimary)
                    }
                    .padding(18)
                    .frame(maxWidth: .infinity, alignment: .leading)
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
