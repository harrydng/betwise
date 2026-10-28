// Models/News/NewsArticle.swift

import Foundation

struct NewsArticle: Codable, Identifiable, Hashable {
    let id: Int
    let category: Category
    let headline: String
    let summary: String
    let source: String
    let publishedAt: Date
    let tickers: [String]

    enum Category: String, Codable, CaseIterable, Hashable {
        case markets = "MARKETS"
        case economy = "ECONOMY"
        case tech = "TECH"
        case earnings = "EARNINGS"

        var title: String {
            switch self {
            case .markets: String(localized: "Markets")
            case .economy: String(localized: "Economy")
            case .tech: String(localized: "Tech")
            case .earnings: String(localized: "Earnings")
            }
        }

        var symbolName: String {
            switch self {
            case .markets: "chart.xyaxis.line"
            case .economy: "building.columns"
            case .tech: "cpu"
            case .earnings: "doc.text.magnifyingglass"
            }
        }
    }
}
