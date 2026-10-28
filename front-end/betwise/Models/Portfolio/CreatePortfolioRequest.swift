// Models/Portfolio/CreatePortfolioRequest.swift

import Foundation

struct CreatePortfolioRequest: Codable {
    let name: String
    let startingBalance: Decimal
}
