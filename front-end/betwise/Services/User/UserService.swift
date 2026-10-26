// Services/User/UserService.swift

import Foundation

final class UserService {
    static let shared = UserService()

    private let api = APIClient.shared

    private init() {}

    func getCurrentUser() async throws -> User {
        try await api.request(
            path: "/api/users/me",
            method: "GET"
        )
    }
}
