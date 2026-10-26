// Services/Watchlist/WatchlistService.swift

import Foundation

final class WatchlistService {
    static let shared = WatchlistService()

    private let api = APIClient.shared

    private init() {}

    func getWatchlists() async throws -> [Watchlist] {
        try await api.request(
            path: "/api/watchlists",
            method: "GET"
        )
    }

    func createWatchlist(
        name: String
    ) async throws -> Watchlist {
        try await api.request(
            path: "/api/watchlists",
            method: "POST",
            body: CreateWatchlistRequest(
                name: name
            )
        )
    }

    func getWatchlist(
        id: Int
    ) async throws -> Watchlist {
        try await api.request(
            path: "/api/watchlists/\(id)",
            method: "GET"
        )
    }

    func deleteWatchlist(
        id: Int
    ) async throws {
        let _: EmptyResponse = try await api.request(
            path: "/api/watchlists/\(id)",
            method: "DELETE"
        )
    }

    func addItem(
        watchlistId: Int,
        symbol: String
    ) async throws -> WatchlistItem {
        try await api.request(
            path: "/api/watchlists/\(watchlistId)/items",
            method: "POST",
            body: AddWatchlistItemRequest(
                symbol: symbol
            )
        )
    }

    func deleteItem(
        watchlistId: Int,
        itemId: Int
    ) async throws {
        let _: EmptyResponse = try await api.request(
            path: "/api/watchlists/\(watchlistId)/items/\(itemId)",
            method: "DELETE"
        )
    }
}
