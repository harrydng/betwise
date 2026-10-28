// Models/Portfolio/Holding.swift

import Foundation

struct Holding: Codable, Identifiable, Hashable {
    let id: Int
    let assetId: Int
    let ticker: String
    let name: String
    let assetType: Asset.AssetType
    let quantity: Decimal
    let averageCost: Decimal
    let currentPrice: Decimal
    let costBasis: Decimal
    let marketValue: Decimal
    let unrealizedGain: Decimal
    let unrealizedGainPercent: Decimal
}
