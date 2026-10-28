import SwiftUI

struct NewsView: View {
    let model: NewsModel

    @State private var category: NewsArticle.Category?

    private static let categoryOptions: [FilterOption<NewsArticle.Category?>] =
        [FilterOption(value: nil, title: String(localized: "Top stories"))]
        + NewsArticle.Category.allCases.map { FilterOption(value: $0, title: $0.title) }

    var body: some View {
        // A feed of a few dozen stories at most, so filtering inline is cheap.
        let articles = model.articles
        let visible = category.map { category in articles.filter { $0.category == category } } ?? articles

        ScrollView {
            VStack(alignment: .leading, spacing: 24) {
                FilterChips(options: Self.categoryOptions, selection: $category)

                if !model.hasLoaded {
                    ProgressView()
                        .frame(maxWidth: .infinity, minHeight: 240)
                } else if visible.isEmpty {
                    ContentUnavailableView(
                        "No stories yet",
                        systemImage: "newspaper",
                        description: Text("Market news will appear here.")
                    )
                    .card()
                }

                if let featured = visible.first {
                    NavigationLink(value: featured) {
                        FeaturedStoryCard(article: featured)
                    }
                    .buttonStyle(.plain)
                }

                if visible.count > 1 {
                    VStack(spacing: 0) {
                        ForEach(visible.dropFirst()) { article in
                            NavigationLink(value: article) {
                                StoryRow(article: article)
                            }
                            .buttonStyle(.plain)

                            if article.id != visible.last?.id {
                                Divider().overlay(AppColors.stroke)
                            }
                        }
                    }
                    .card(padding: 16)
                }
            }
            .padding(.horizontal, 20)
            .padding(.bottom, 32)
        }
        .scrollIndicators(.hidden)
        .screenBackground()
        .navigationTitle("News")
        .navigationDestination(for: NewsArticle.self) { article in
            NewsArticleView(article: article)
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

private struct StoryArtwork: View {
    let category: NewsArticle.Category
    var cornerRadius: CGFloat = 16

    var body: some View {
        ZStack {
            LinearGradient(
                colors: [AppColors.accent.opacity(0.85), AppColors.accentDeep],
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )

            Image(systemName: category.symbolName)
                .font(.system(size: 34, weight: .semibold))
                .foregroundStyle(.white.opacity(0.9))
        }
        .clipShape(.rect(cornerRadius: cornerRadius, style: .continuous))
        .accessibilityHidden(true)
    }
}

private struct StoryMeta: View {
    let source: String
    let publishedAt: Date

    var body: some View {
        HStack(spacing: 4) {
            Text(source)
            Text("·")
            Text(publishedAt, format: .relative(presentation: .named))
        }
        .font(.caption)
        .foregroundStyle(AppColors.textSecondary)
    }
}

private struct FeaturedStoryCard: View {
    let article: NewsArticle

    var body: some View {
        VStack(alignment: .leading, spacing: 14) {
            StoryArtwork(category: article.category, cornerRadius: 18)
                .frame(height: 170)

            Text(article.category.title.uppercased())
                .font(.caption.weight(.bold))
                .tracking(1)
                .foregroundStyle(AppColors.accentBright)

            Text(article.headline)
                .font(.title3.weight(.bold))
                .foregroundStyle(AppColors.textPrimary)
                .fixedSize(horizontal: false, vertical: true)

            StoryMeta(source: article.source, publishedAt: article.publishedAt)
        }
        .card(padding: 14)
        .contentShape(.rect)
        .accessibilityElement(children: .combine)
    }
}

private struct StoryRow: View {
    let article: NewsArticle

    var body: some View {
        HStack(alignment: .top, spacing: 14) {
            VStack(alignment: .leading, spacing: 8) {
                Text(article.headline)
                    .font(.subheadline.weight(.semibold))
                    .foregroundStyle(AppColors.textPrimary)
                    .lineLimit(3)

                StoryMeta(source: article.source, publishedAt: article.publishedAt)

                TickerTags(tickers: article.tickers)
            }

            Spacer(minLength: 0)

            StoryArtwork(category: article.category, cornerRadius: 12)
                .frame(width: 72, height: 72)
        }
        .padding(.vertical, 12)
        .contentShape(.rect)
        .accessibilityElement(children: .combine)
    }
}

private struct TickerTags: View {
    let tickers: [String]

    var body: some View {
        HStack(spacing: 6) {
            ForEach(tickers, id: \.self) { ticker in
                Text(ticker)
                    .font(.caption2.weight(.bold))
                    .foregroundStyle(AppColors.accentBright)
                    .padding(.horizontal, 7)
                    .padding(.vertical, 3)
                    .background(AppColors.accent.opacity(0.14), in: Capsule())
            }
        }
    }
}

private struct NewsArticleView: View {
    let article: NewsArticle

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 18) {
                StoryArtwork(category: article.category, cornerRadius: 22)
                    .frame(height: 200)

                Text(article.category.title.uppercased())
                    .font(.caption.weight(.bold))
                    .tracking(1)
                    .foregroundStyle(AppColors.accentBright)

                Text(article.headline)
                    .font(.title.weight(.bold))
                    .foregroundStyle(AppColors.textPrimary)
                    .fixedSize(horizontal: false, vertical: true)

                StoryMeta(source: article.source, publishedAt: article.publishedAt)

                TickerTags(tickers: article.tickers)

                Text(article.summary)
                    .font(.body)
                    .foregroundStyle(AppColors.textSecondary)
                    .fixedSize(horizontal: false, vertical: true)
            }
            .padding(.horizontal, 20)
            .padding(.bottom, 32)
        }
        .scrollIndicators(.hidden)
        .screenBackground()
        #if os(iOS)
        .navigationBarTitleDisplayMode(.inline)
        #endif
    }
}

#Preview {
    NavigationStack {
        NewsView(model: NewsModel(usesSampleData: true))
    }
    .preferredColorScheme(.dark)
}
