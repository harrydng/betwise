// Models/Portfolio/TransferCashRequest.swift

import Foundation

struct TransferCashRequest: Codable {
    let destinationPortfolioId: Int
    let amount: Decimal
}
