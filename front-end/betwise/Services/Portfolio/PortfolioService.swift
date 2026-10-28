// Services/Portfolio/PortfolioService.swift

import Foundation

final class PortfolioService {
    static let shared = PortfolioService()

    private let api = APIClient.shared

    private init() {}

    func getPortfolios() async throws -> [Portfolio] {
        try await api.request(
            .get,
            "/api/portfolios"
        )
    }

    func getPortfolio(
        id: Int
    ) async throws -> Portfolio {
        try await api.request(
            .get,
            "/api/portfolios/\(id)"
        )
    }

    func createPortfolio(
        name: String,
        startingBalance: Decimal
    ) async throws -> Portfolio {
        try await api.request(
            .post,
            "/api/portfolios",
            body: CreatePortfolioRequest(
                name: name,
                startingBalance: startingBalance
            )
        )
    }

    func renamePortfolio(
        id: Int,
        name: String
    ) async throws -> Portfolio {
        try await api.request(
            .patch,
            "/api/portfolios/\(id)",
            body: UpdatePortfolioRequest(
                name: name
            )
        )
    }

    func deletePortfolio(
        id: Int
    ) async throws {
        try await api.perform(
            .delete,
            "/api/portfolios/\(id)"
        )
    }

    func getHoldings(
        portfolioId: Int
    ) async throws -> [Holding] {
        try await api.request(
            .get,
            "/api/portfolios/\(portfolioId)/holdings"
        )
    }

    /// The portfolio's total value over time, rebuilt from its trades.
    func getHistory(
        portfolioId: Int,
        range: ChartRange
    ) async throws -> PriceHistoryResponse {
        try await api.request(
            .get,
            "/api/portfolios/\(portfolioId)/history",
            query: [URLQueryItem(name: "range", value: range.rawValue)]
        )
    }

    func getTransactions(
        portfolioId: Int
    ) async throws -> [Transaction] {
        try await api.request(
            .get,
            "/api/portfolios/\(portfolioId)/transactions"
        )
    }

    /// Fills at the server's current market price. The returned
    /// transaction shows the price actually used.
    func buy(
        portfolioId: Int,
        assetId: Int,
        quantity: Decimal
    ) async throws -> Transaction {
        try await api.request(
            .post,
            "/api/portfolios/\(portfolioId)/buy",
            body: TradeRequest(
                assetId: assetId,
                quantity: quantity
            )
        )
    }

    /// Fills at the server's current market price. The returned
    /// transaction shows the price actually used.
    func sell(
        portfolioId: Int,
        assetId: Int,
        quantity: Decimal
    ) async throws -> Transaction {
        try await api.request(
            .post,
            "/api/portfolios/\(portfolioId)/sell",
            body: TradeRequest(
                assetId: assetId,
                quantity: quantity
            )
        )
    }

    func transferCash(
        fromPortfolioId: Int,
        toPortfolioId: Int,
        amount: Decimal
    ) async throws {
        try await api.perform(
            .post,
            "/api/portfolios/\(fromPortfolioId)/transfer",
            body: TransferCashRequest(
                destinationPortfolioId: toPortfolioId,
                amount: amount
            )
        )
    }
}
