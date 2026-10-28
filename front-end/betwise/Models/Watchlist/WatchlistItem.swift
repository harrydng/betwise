// Models/Watchlist/WatchlistItem.swift

import Foundation

struct WatchlistItem: Codable, Identifiable, Hashable {
    let id: Int
    let assetId: Int
    let ticker: String
    let name: String
    let assetType: Asset.AssetType
    let addedAt: Date
}
