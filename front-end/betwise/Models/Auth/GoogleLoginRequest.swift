// Models/Auth/GoogleLoginRequest.swift

import Foundation

struct GoogleLoginRequest: Codable {
    let idToken: String
    let nonce: String
}
