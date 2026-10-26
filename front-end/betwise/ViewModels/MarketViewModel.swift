import Foundation
import Combine

@MainActor
final class MarketViewModel: ObservableObject {
    @Published var stocks: [StockPosition] = [
        StockPosition(
            symbol: "AAPL",
            companyName: "Apple",
            purchasePrice: 20,
            currentPrice: 50,
            priceHistory: [20, 24, 22, 30, 28, 36, 35, 42, 50]
        ),
        StockPosition(
            symbol: "NVDA",
            companyName: "NVIDIA",
            purchasePrice: 80,
            currentPrice: 120,
            priceHistory: [80, 82, 90, 87, 95, 103, 99, 111, 120]
        ),
        StockPosition(
            symbol: "TSLA",
            companyName: "Tesla",
            purchasePrice: 260,
            currentPrice: 238,
            priceHistory: [260, 252, 248, 254, 245, 241, 247, 239, 238]
        )
    ]

    @Published var watchlists: [Watchlist] = []
    @Published var isLoading = false
    @Published var errorMessage: String?

    func loadWatchlists() async {
        isLoading = true
        errorMessage = nil

        do {
            watchlists = try await WatchlistService.shared.getWatchlists()
        } catch {
            errorMessage = error.localizedDescription
        }

        isLoading = false
    }
}
