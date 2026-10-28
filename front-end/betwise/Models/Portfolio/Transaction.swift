// Models/Portfolio/Transaction.swift

import Foundation

struct Transaction: Codable, Identifiable, Hashable {
    let id: Int
    let assetId: Int
    let ticker: String
    let transactionType: TransactionType
    let quantity: Decimal
    let price: Decimal
    let totalAmount: Decimal
    let executedAt: Date

    enum TransactionType: String, Codable, CaseIterable {
        case buy = "BUY"
        case sell = "SELL"
    }
}
