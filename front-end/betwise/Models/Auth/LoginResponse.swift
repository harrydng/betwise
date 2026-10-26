// Models/Auth/LoginResponse.swift

import Foundation

struct LoginResponse: Codable {
    let token: String?
    let accessToken: String?
    let refreshToken: String?
    let user: User?

    var bearerToken: String? {
        accessToken ?? token
    }
}
