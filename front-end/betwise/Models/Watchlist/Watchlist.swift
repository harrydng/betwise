// Models/Watchlist/Watchlist.swift

import Foundation

struct Watchlist: Codable, Identifiable, Hashable {
    let id: Int
    let name: String
    let createdAt: Date
    let updatedAt: Date
    let items: [WatchlistItem]
}
