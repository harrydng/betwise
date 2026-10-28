// Models/Asset/Asset.swift

import Foundation

struct Asset: Codable, Identifiable, Hashable {
    let id: Int
    let ticker: String
    let name: String
    let assetType: AssetType
    let currentPrice: Decimal

    /// Today's move in percent, where 2.5 means +2.5%.
    var dayChangePercent: Decimal?

    /// Today's prices, thinned out for small charts.
    var sparkline: [Decimal]?

    enum AssetType: String, Codable, CaseIterable {
        case stock = "STOCK"
        case etf = "ETF"
        case bond = "BOND"
        case reit = "REIT"
    }
}
