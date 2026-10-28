// Models/User/User.swift

import Foundation

struct User: Codable, Identifiable, Hashable {
    let id: Int
    let name: String
    let email: String
    let dateOfBirth: Date?
    let experienceLevel: ExperienceLevel?
    let investmentGoal: InvestmentGoal?
    let riskTolerance: RiskTolerance?
    let createdAt: Date
    let updatedAt: Date

    enum ExperienceLevel: String, Codable, CaseIterable {
        case beginner = "BEGINNER"
        case intermediate = "INTERMEDIATE"
        case advanced = "ADVANCED"
    }

    enum InvestmentGoal: String, Codable, CaseIterable {
        case capitalPreservation = "CAPITAL_PRESERVATION"
        case income = "INCOME"
        case longTermGrowth = "LONG_TERM_GROWTH"
        case shortTermGrowth = "SHORT_TERM_GROWTH"
    }

    enum RiskTolerance: String, Codable, CaseIterable {
        case conservative = "CONSERVATIVE"
        case balanced = "BALANCED"
        case aggressive = "AGGRESSIVE"
    }
}
