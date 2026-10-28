// Models/Market/MarketRowItem.swift

import Foundation

/// Everything a market list row or asset screen shows for one asset,
/// prepared once so views don't recompute it on every update.
struct MarketRowItem: Identifiable, Hashable {
    /// The asset's id.
    let id: Int
    let ticker: String
    let name: String
    let assetType: Asset.AssetType
    let price: Decimal
    let dayChangePercent: Double
    let sparkline: [PricePoint]
    let dayLow: Double
    let dayHigh: Double
}
