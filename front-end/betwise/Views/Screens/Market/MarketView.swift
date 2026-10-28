import SwiftUI

struct MarketView: View {
    @Bindable var model: MarketModel
    let portfolio: PortfolioModel

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 28) {
                IndexStrip(indices: model.indices)

                if !model.watchlistRows.isEmpty {
                    WatchlistSection(rows: model.watchlistRows)
                }

                VStack(alignment: .leading, spacing: 14) {
                    SectionHeader(title: "Explore")

                    if !model.hasLoaded {
                        ProgressView()
                            .frame(maxWidth: .infinity, minHeight: 160)
                    } else if model.rows.isEmpty {
                        ContentUnavailableView(
                            "No assets yet",
                            systemImage: "chart.line.uptrend.xyaxis",
                            description: Text("Stocks and funds will appear here once they're added to Betwise.")
                        )
                        .card()
                    } else {
                        FilterChips(options: MarketModel.filterOptions, selection: $model.filter)

                        AssetList(rows: model.visibleRows)
                    }
                }
            }
            .padding(.horizontal, 20)
            .padding(.bottom, 32)
        }
        .scrollIndicators(.hidden)
        .screenBackground()
        .navigationTitle("Market")
        .assetDestinations(market: model, portfolio: portfolio)
        .refreshable {
            await model.refresh()
        }
        .task {
            await model.refresh()
        }
    }
}

// MARK: - Sections

private struct IndexStrip: View {
    let indices: [MarketIndex]

    var body: some View {
        ScrollView(.horizontal) {
            HStack(spacing: 12) {
                ForEach(indices) { index in
                    IndexCard(
                        name: index.name,
                        value: index.value,
                        changePercent: index.changePercent,
                        sparkline: index.sparkline
                    )
                }
            }
        }
        .scrollIndicators(.hidden)
        .contentMargins(.horizontal, 20, for: .scrollContent)
        .padding(.horizontal, -20)
    }
}

private struct IndexCard: View {
    let name: String
    let value: Double
    let changePercent: Double
    let sparkline: [PricePoint]

    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            Text(name)
                .font(.caption.weight(.semibold))
                .foregroundStyle(AppColors.textSecondary)

            Text(value, format: .number.precision(.fractionLength(2)))
                .font(.headline)
                .monospacedDigit()
                .foregroundStyle(AppColors.textPrimary)

            Sparkline(points: sparkline, isGain: changePercent >= 0)
                .frame(height: 30)

            ChangeBadge(percent: changePercent)
        }
        .frame(width: 150, alignment: .leading)
        .card(padding: 14, cornerRadius: 20)
        .accessibilityElement(children: .combine)
    }
}

private struct WatchlistSection: View {
    let rows: [MarketRowItem]

    var body: some View {
        VStack(alignment: .leading, spacing: 14) {
            SectionHeader(title: "Watchlist", detail: rows.count.formatted())

            ScrollView(.horizontal) {
                HStack(spacing: 12) {
                    ForEach(rows) { row in
                        NavigationLink(value: row) {
                            WatchlistCard(
                                ticker: row.ticker,
                                name: row.name,
                                price: row.price,
                                changePercent: row.dayChangePercent,
                                sparkline: row.sparkline
                            )
                        }
                        .buttonStyle(.plain)
                    }
                }
            }
            .scrollIndicators(.hidden)
            .contentMargins(.horizontal, 20, for: .scrollContent)
            .padding(.horizontal, -20)
        }
    }
}

private struct WatchlistCard: View {
    let ticker: String
    let name: String
    let price: Decimal
    let changePercent: Double
    let sparkline: [PricePoint]

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack(spacing: 10) {
                TickerLogo(ticker: ticker, size: 34)

                VStack(alignment: .leading, spacing: 1) {
                    Text(ticker)
                        .font(.subheadline.weight(.bold))
                        .foregroundStyle(AppColors.textPrimary)

                    Text(name)
                        .font(.caption2)
                        .foregroundStyle(AppColors.textSecondary)
                        .lineLimit(1)
                }
            }

            Sparkline(points: sparkline, isGain: changePercent >= 0)
                .frame(height: 38)

            HStack(alignment: .firstTextBaseline) {
                Text(price, format: .currency(code: "USD"))
                    .font(.subheadline.weight(.semibold))
                    .monospacedDigit()
                    .foregroundStyle(AppColors.textPrimary)

                Spacer(minLength: 4)

                ChangeBadge(percent: changePercent)
            }
        }
        .frame(width: 190, alignment: .leading)
        .card(padding: 14, cornerRadius: 20)
        .contentShape(.rect)
        .accessibilityElement(children: .combine)
    }
}

private struct AssetList: View {
    let rows: [MarketRowItem]

    var body: some View {
        VStack(spacing: 0) {
            ForEach(rows) { row in
                NavigationLink(value: row) {
                    AssetRow(
                        ticker: row.ticker,
                        name: row.name,
                        price: row.price,
                        changePercent: row.dayChangePercent,
                        sparkline: row.sparkline
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

#Preview {
    NavigationStack {
        MarketView(model: MarketModel(usesSampleData: true), portfolio: PortfolioModel(usesSampleData: true))
    }
    .preferredColorScheme(.dark)
}
