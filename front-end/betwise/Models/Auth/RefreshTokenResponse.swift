// Models/Auth/RefreshTokenResponse.swift

import Foundation

/// The refresh endpoint only issues a new access token; the refresh token
/// stays the same.
struct RefreshTokenResponse: Codable {
    let accessToken: String
}
