// Services/User/UserService.swift

import Foundation

final class UserService {
    static let shared = UserService()

    private let api = APIClient.shared

    private init() {}

    func createUser(
        _ request: CreateUserRequest
    ) async throws -> User {
        try await api.request(
            .post,
            "/api/users",
            body: request,
            authenticated: false
        )
    }

    func getCurrentUser() async throws -> User {
        try await api.request(
            .get,
            "/api/users/me"
        )
    }

    func updateCurrentUser(
        _ request: UpdateUserRequest
    ) async throws -> User {
        try await api.request(
            .patch,
            "/api/users/me",
            body: request
        )
    }

    func changePassword(
        currentPassword: String,
        newPassword: String
    ) async throws {
        try await api.perform(
            .patch,
            "/api/users/me/password",
            body: UpdatePasswordRequest(
                currentPassword: currentPassword,
                newPassword: newPassword
            )
        )
    }

    /// Deletes the account and signs out.
    func deleteCurrentUser() async throws {
        try await api.perform(
            .delete,
            "/api/users/me"
        )

        api.clearTokens()
    }
}
