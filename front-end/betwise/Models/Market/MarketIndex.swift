// Models/Market/MarketIndex.swift

import Foundation

struct MarketIndex: Identifiable, Hashable {
    let id: String
    let name: String
    let value: Double
    let changePercent: Double
    let sparkline: [PricePoint]
}
