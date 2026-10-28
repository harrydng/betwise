// Models/Portfolio/Portfolio.swift

import Foundation

struct Portfolio: Codable, Identifiable, Hashable {
    let id: Int
    let name: String
    let startingBalance: Decimal
    let cashAvailable: Decimal
    let holdingsValue: Decimal
    let currentPortfolioValue: Decimal
    let investedCostBasis: Decimal
    let unrealizedGain: Decimal
    let unrealizedGainPercent: Decimal
    let createdAt: Date
    let updatedAt: Date
    let holdings: [Holding]
}
