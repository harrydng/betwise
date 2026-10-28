// Models/Lesson/Lesson.swift

import Foundation

struct Lesson: Codable, Identifiable, Hashable {
    let id: Int
    let title: String
    let description: String?
    let category: String
    let categorySlug: String
    let topic: Topic
    let difficulty: Difficulty
    let cashReward: Decimal

    /// How many sections the lesson has. Missing from older backends.
    var checkpointCount: Int?
    var position: Int?

    enum Topic: String, Codable, CaseIterable {
        case stocks = "STOCKS"
        case bonds = "BONDS"
        case etfs = "ETFS"
        case diversification = "DIVERSIFICATION"
        case risk = "RISK"
        case shortSelling = "SHORT_SELLING"
        case fundamentalAnalysis = "FUNDAMENTAL_ANALYSIS"
        case technicalAnalysis = "TECHNICAL_ANALYSIS"
        case realEstate = "REAL_ESTATE"
    }

    enum Difficulty: String, Codable, CaseIterable {
        case beginner = "BEGINNER"
        case intermediate = "INTERMEDIATE"
        case advanced = "ADVANCED"
    }
}
