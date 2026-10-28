// ViewModels/PortfolioModel.swift

import Foundation
import Observation

@Observable
final class PortfolioModel {
    private(set) var portfolioID: Int?
    private(set) var totalValue: Decimal = 0
    private(set) var cashAvailable: Decimal = 0
    private(set) var holdingsValue: Decimal = 0
    private(set) var unrealizedGain: Decimal = 0
    private(set) var unrealizedGainPercent: Double = 0
    private(set) var holdings: [Holding] = []
    private(set) var allocation: [AllocationSlice] = []
    private(set) var transactions: [Transaction] = []
    private(set) var history: [PricePoint] = []

    /// Guests see a sample portfolio; signed-in users only ever see theirs.
    let isShowingSampleData: Bool

    /// False for a signed-in user who hasn't created a portfolio yet.
    private(set) var hasPortfolio: Bool
    private(set) var hasLoaded: Bool

    private(set) var errorMessage: String?

    var range: ChartRange = .month {
        didSet {
            if isShowingSampleData {
                rebuildSampleHistory()
            } else {
                Task { await loadHistory() }
            }
        }
    }

    private var startingBalance: Decimal = 0

    init(usesSampleData: Bool) {
        isShowingSampleData = usesSampleData
        hasPortfolio = usesSampleData
        hasLoaded = usesSampleData

        if usesSampleData {
            apply(SampleData.portfolio)
            transactions = SampleData.transactions
        }
    }

    func holding(forAssetID assetID: Int) -> Holding? {
        holdings.first { $0.assetId == assetID }
    }

    /// Loads the user's first portfolio.
    func refresh() async {
        guard !isShowingSampleData else { return }

        do {
            if let portfolio = try await PortfolioService.shared.getPortfolios().first {
                apply(portfolio)
                transactions = try await PortfolioService.shared.getTransactions(portfolioId: portfolio.id)
                hasPortfolio = true
                await loadHistory()
            } else {
                hasPortfolio = false
            }

            errorMessage = nil
        } catch {
            errorMessage = error.localizedDescription
        }

        hasLoaded = true
    }

    /// Creates the user's first portfolio with practice money.
    func createPortfolio(name: String, startingBalance: Decimal) async throws {
        let portfolio = try await PortfolioService.shared.createPortfolio(
            name: name,
            startingBalance: startingBalance
        )

        apply(portfolio)
        transactions = []
        hasPortfolio = true
        await loadHistory()
    }

    private func apply(_ portfolio: Portfolio) {
        portfolioID = portfolio.id
        startingBalance = portfolio.startingBalance
        totalValue = portfolio.currentPortfolioValue
        cashAvailable = portfolio.cashAvailable
        holdingsValue = portfolio.holdingsValue
        unrealizedGain = portfolio.unrealizedGain
        unrealizedGainPercent = portfolio.unrealizedGainPercent.doubleValue
        holdings = portfolio.holdings.sorted { $0.marketValue > $1.marketValue }
        allocation = Self.makeAllocation(holdings: holdings, cash: cashAvailable, total: totalValue)

        if isShowingSampleData {
            rebuildSampleHistory()
        }
    }

    /// Fetches the chart for the current range from the backend.
    private func loadHistory() async {
        guard let portfolioID else { return }

        let requestedRange = range

        do {
            let response = try await PortfolioService.shared.getHistory(
                portfolioId: portfolioID,
                range: requestedRange
            )

            // Drop the result if the user has already picked another range.
            guard requestedRange == range else { return }

            history = response.chartPoints
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    private func rebuildSampleHistory() {
        let total = totalValue.doubleValue
        let start = startingBalance.doubleValue

        // "All time" is measured against the starting balance, so it
        // matches the real overall return.
        let change = range == .all && start > 0
            ? (total - start) / start * 100
            : PriceHistory.changePercent(seed: "portfolio", range: range, dayChange: 0.86)

        history = PriceHistory.series(
            seed: "portfolio",
            range: range,
            endValue: total,
            changePercent: change
        )
    }

    private static func makeAllocation(
        holdings: [Holding],
        cash: Decimal,
        total: Decimal
    ) -> [AllocationSlice] {
        let total = total.doubleValue

        guard total > 0 else { return [] }

        var slices = holdings.map { holding in
            AllocationSlice(
                id: holding.ticker,
                label: holding.ticker,
                fraction: holding.marketValue.doubleValue / total,
                isCash: false
            )
        }

        if cash > 0 {
            slices.append(
                AllocationSlice(
                    id: "cash",
                    label: String(localized: "Cash"),
                    fraction: cash.doubleValue / total,
                    isCash: true
                )
            )
        }

        return slices
    }
}
