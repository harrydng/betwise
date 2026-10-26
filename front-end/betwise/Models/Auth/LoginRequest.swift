// Models/Auth/LoginRequest.swift

import Foundation

struct LoginRequest: Codable {
    let email: String
    let password: String
}
