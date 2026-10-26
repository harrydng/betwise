// Models/Stock/StockPosition.swift

import Foundation

struct StockPosition: Identifiable, Hashable {
    let id = UUID()
    let symbol: String
    let companyName: String
    let purchasePrice: Double
    let currentPrice: Double
    let priceHistory: [Double]

    var percentageChange: Double {
        guard purchasePrice != 0 else { return 0 }

        return ((currentPrice - purchasePrice) / purchasePrice) * 100
    }

    var isPositive: Bool {
        percentageChange >= 0
    }
}
