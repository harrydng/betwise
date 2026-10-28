// Models/User/CreateUserRequest.swift

import Foundation

struct CreateUserRequest: Codable {
    let name: String
    let email: String
    let password: String
    var dateOfBirth: Date?
    var experienceLevel: User.ExperienceLevel?
    var investmentGoal: User.InvestmentGoal?
    var riskTolerance: User.RiskTolerance?
}
