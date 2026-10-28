// Models/Portfolio/TradeRequest.swift

import Foundation

/// Used for both buying and selling. The server fills the order at its own
/// current market price.
struct TradeRequest: Codable {
    let assetId: Int
    let quantity: Decimal
}
