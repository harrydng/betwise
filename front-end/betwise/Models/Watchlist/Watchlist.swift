// Models/Watchlist/Watchlist.swift

import Foundation

struct Watchlist: Codable, Identifiable {
    let id: Int
    let name: String
    let items: [WatchlistItem]?
}
