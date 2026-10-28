import SwiftUI

struct AssetDetailView: View {
    let item: MarketRowItem
    let market: MarketModel
    let portfolio: PortfolioModel

    @State private var range: ChartRange = .day
    @State private var history: [PricePoint] = []
    @State private var selectedIndex: Int?
    @State private var tradeSide: TradeSide?

    var body: some View {
        let holding = portfolio.holding(forAssetID: item.id)
        let isRangeGain = (history.last?.value ?? 0) >= (history.first?.value ?? 0)

        ScrollView {
            VStack(alignment: .leading, spacing: 24) {
                AssetTitle(ticker: item.ticker, name: item.name, assetType: item.assetType)

                VStack(alignment: .leading, spacing: 18) {
                    ChartValueHeader(
                        title: "Price",
                        currentValue: item.price,
                        points: history,
                        selectedIndex: selectedIndex,
                        range: range
                    )

                    PriceChart(
                        points: history,
                        tint: isRangeGain ? AppColors.gain : AppColors.loss,
                        selectedIndex: $selectedIndex
                    )
                    .frame(height: 230)

                    RangePicker(selection: $range)
                }

                if let holding {
                    PositionCard(
                        quantity: holding.quantity,
                        averageCost: holding.averageCost,
                        marketValue: holding.marketValue,
                        gain: holding.unrealizedGain,
                        gainPercent: holding.unrealizedGainPercent.doubleValue
                    )
                }

                KeyStatsCard(
                    assetType: item.assetType,
                    price: item.price,
                    dayLow: item.dayLow,
                    dayHigh: item.dayHigh
                )
            }
            .padding(.horizontal, 20)
            .padding(.bottom, 24)
        }
        .scrollIndicators(.hidden)
        .screenBackground()
        .navigationTitle(item.ticker)
        #if os(iOS)
        .navigationBarTitleDisplayMode(.inline)
        #endif
        .toolbar {
            ToolbarItem(placement: .primaryAction) {
                WatchlistButton(assetID: item.id, market: market)
            }
        }
        .safeAreaInset(edge: .bottom) {
            TradeBar(canSell: holding != nil) { side in
                tradeSide = side
            }
        }
        .sheet(item: $tradeSide) { side in
            TradeView(
                assetID: item.id,
                ticker: item.ticker,
                price: item.price,
                initialSide: side,
                portfolio: portfolio
            )
        }
        // Reruns, and cancels the previous load, whenever the range changes.
        .task(id: range) {
            selectedIndex = nil
            await loadHistory()
        }
    }

    private func loadHistory() async {
        guard !market.usesSampleData else {
            history = PriceHistory.series(
                seed: item.ticker,
                range: range,
                endValue: item.price.doubleValue,
                changePercent: PriceHistory.changePercent(
                    seed: item.ticker,
                    range: range,
                    dayChange: item.dayChangePercent
                )
            )
            return
        }

        // Keep the previous chart up until the new one arrives, so the
        // screen doesn't flash empty.
        if let response = try? await AssetService.shared.getHistory(assetId: item.id, range: range),
           !Task.isCancelled {
            history = response.chartPoints
        }
    }
}

// MARK: - Sections

private struct AssetTitle: View {
    let ticker: String
    let name: String
    let assetType: Asset.AssetType

    var body: some View {
        HStack(spacing: 14) {
            TickerLogo(ticker: ticker, size: 52)

            VStack(alignment: .leading, spacing: 4) {
                Text(name)
                    .font(.title2.weight(.bold))
                    .foregroundStyle(AppColors.textPrimary)

                Text("\(ticker) · \(assetType.displayName)")
                    .font(.subheadline)
                    .foregroundStyle(AppColors.textSecondary)
            }
        }
        .accessibilityElement(children: .combine)
    }
}

private struct WatchlistButton: View {
    let assetID: Int
    let market: MarketModel

    var body: some View {
        let isWatching = market.isWatching(assetID)

        Button {
            Task {
                await market.toggleWatchlist(assetID)
            }
        } label: {
            Label(
                isWatching ? "Remove from watchlist" : "Add to watchlist",
                systemImage: isWatching ? "star.fill" : "star"
            )
        }
        .tint(isWatching ? AppColors.accentBright : AppColors.textPrimary)
        .sensoryFeedback(.selection, trigger: isWatching)
    }
}

private struct PositionCard: View {
    let quantity: Decimal
    let averageCost: Decimal
    let marketValue: Decimal
    let gain: Decimal
    let gainPercent: Double

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("Your position")
                .font(.headline)
                .foregroundStyle(AppColors.textPrimary)

            Grid(alignment: .leading, horizontalSpacing: 16, verticalSpacing: 14) {
                GridRow {
                    StatCell(title: "Shares") {
                        Text(quantity.formatted())
                    }
                    StatCell(title: "Market value") {
                        Text(marketValue, format: .currency(code: "USD"))
                    }
                }

                GridRow {
                    StatCell(title: "Average cost") {
                        Text(averageCost, format: .currency(code: "USD"))
                    }
                    StatCell(title: "Total return") {
                        Text("\(gain, format: .currency(code: "USD").sign(strategy: .always())) (\(gainPercent / 100, format: .percent.precision(.fractionLength(2))))")
                            .foregroundStyle(AppColors.movement(gainPercent))
                    }
                }
            }
        }
        .card()
    }
}

private struct KeyStatsCard: View {
    let assetType: Asset.AssetType
    let price: Decimal
    let dayLow: Double
    let dayHigh: Double

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("Key stats")
                .font(.headline)
                .foregroundStyle(AppColors.textPrimary)

            Grid(alignment: .leading, horizontalSpacing: 16, verticalSpacing: 14) {
                GridRow {
                    StatCell(title: "Price") {
                        Text(price, format: .currency(code: "USD"))
                    }
                    StatCell(title: "Type") {
                        Text(assetType.displayName)
                    }
                }

                GridRow {
                    StatCell(title: "Day low") {
                        Text(dayLow, format: .currency(code: "USD"))
                    }
                    StatCell(title: "Day high") {
                        Text(dayHigh, format: .currency(code: "USD"))
                    }
                }
            }
        }
        .card()
    }
}

private struct StatCell<Value: View>: View {
    let title: LocalizedStringKey
    @ViewBuilder let value: Value

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(title)
                .font(.caption)
                .foregroundStyle(AppColors.textSecondary)

            value
                .font(.subheadline.weight(.semibold))
                .monospacedDigit()
                .foregroundStyle(AppColors.textPrimary)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .accessibilityElement(children: .combine)
    }
}

private struct TradeBar: View {
    let canSell: Bool
    let onTrade: (TradeSide) -> Void

    var body: some View {
        HStack(spacing: 12) {
            if canSell {
                Button {
                    onTrade(.sell)
                } label: {
                    Text("Sell")
                        .font(.headline)
                        .frame(maxWidth: .infinity)
                }
                .buttonStyle(.glass)
            }

            Button {
                onTrade(.buy)
            } label: {
                Text("Buy")
                    .font(.headline)
                    .frame(maxWidth: .infinity)
            }
            .buttonStyle(.glassProminent)
            .tint(AppColors.accent)
        }
        .controlSize(.large)
        .buttonBorderShape(.capsule)
        .padding(.horizontal, 20)
        .padding(.vertical, 10)
    }
}

#Preview {
    let market = MarketModel(usesSampleData: true)

    NavigationStack {
        AssetDetailView(
            item: market.rows[1],
            market: market,
            portfolio: PortfolioModel(usesSampleData: true)
        )
    }
    .preferredColorScheme(.dark)
}
