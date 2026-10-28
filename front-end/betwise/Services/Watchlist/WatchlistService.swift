// Services/Watchlist/WatchlistService.swift

import Foundation

final class WatchlistService {
    static let shared = WatchlistService()

    private let api = APIClient.shared

    private init() {}

    func getWatchlists() async throws -> [Watchlist] {
        try await api.request(
            .get,
            "/api/watchlists"
        )
    }

    func getWatchlist(
        id: Int
    ) async throws -> Watchlist {
        try await api.request(
            .get,
            "/api/watchlists/\(id)"
        )
    }

    func createWatchlist(
        name: String
    ) async throws -> Watchlist {
        try await api.request(
            .post,
            "/api/watchlists",
            body: CreateWatchlistRequest(
                name: name
            )
        )
    }

    func renameWatchlist(
        id: Int,
        name: String
    ) async throws -> Watchlist {
        try await api.request(
            .patch,
            "/api/watchlists/\(id)",
            body: UpdateWatchlistRequest(
                name: name
            )
        )
    }

    func deleteWatchlist(
        id: Int
    ) async throws {
        try await api.perform(
            .delete,
            "/api/watchlists/\(id)"
        )
    }

    /// Returns the whole updated watchlist, not just the new item.
    func addItem(
        watchlistId: Int,
        assetId: Int
    ) async throws -> Watchlist {
        try await api.request(
            .post,
            "/api/watchlists/\(watchlistId)/items",
            body: AddWatchlistItemRequest(
                assetId: assetId
            )
        )
    }

    func removeItem(
        watchlistId: Int,
        itemId: Int
    ) async throws {
        try await api.perform(
            .delete,
            "/api/watchlists/\(watchlistId)/items/\(itemId)"
        )
    }
}
