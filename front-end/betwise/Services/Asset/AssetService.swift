// Services/Asset/AssetService.swift

import Foundation

final class AssetService {
    static let shared = AssetService()

    private let api = APIClient.shared

    private init() {}

    func getAssets() async throws -> [Asset] {
        try await api.request(
            .get,
            "/api/assets"
        )
    }

    func getAsset(
        id: Int
    ) async throws -> Asset {
        try await api.request(
            .get,
            "/api/assets/\(id)"
        )
    }

    func getHistory(
        assetId: Int,
        range: ChartRange
    ) async throws -> PriceHistoryResponse {
        try await api.request(
            .get,
            "/api/assets/\(assetId)/history",
            query: [URLQueryItem(name: "range", value: range.rawValue)]
        )
    }

    func searchAssets(
        query: String
    ) async throws -> [Asset] {
        try await api.request(
            .get,
            "/api/assets/search",
            query: [URLQueryItem(name: "query", value: query)]
        )
    }
}
