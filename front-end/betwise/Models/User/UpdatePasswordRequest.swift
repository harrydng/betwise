// Models/User/UpdatePasswordRequest.swift

import Foundation

struct UpdatePasswordRequest: Codable {
    let currentPassword: String
    let newPassword: String
}
