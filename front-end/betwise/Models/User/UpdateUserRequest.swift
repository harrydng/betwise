// Models/User/UpdateUserRequest.swift

import Foundation

/// Leave a field `nil` to keep its current value. The server only updates
/// fields that are present.
struct UpdateUserRequest: Codable {
    var name: String?
    var dateOfBirth: Date?
    var experienceLevel: User.ExperienceLevel?
    var investmentGoal: User.InvestmentGoal?
    var riskTolerance: User.RiskTolerance?
}
