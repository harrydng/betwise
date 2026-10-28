// Services/Auth/AuthService.swift

import Foundation

final class AuthService {
    static let shared = AuthService()

    private let api = APIClient.shared

    private init() {}

    var isSignedIn: Bool {
        api.isSignedIn
    }

    /// Logs in and stores both tokens. Access tokens refresh automatically
    /// from then on; see `APIClient`.
    func login(
        email: String,
        password: String
    ) async throws -> User {
        let response: LoginResponse = try await api.request(
            .post,
            "/api/auth/login",
            body: LoginRequest(
                email: email,
                password: password
            ),
            authenticated: false
        )

        api.saveTokens(
            accessToken: response.accessToken,
            refreshToken: response.refreshToken
        )

        return response.user
    }

    /// Creates an account, then logs in with it.
    func signUp(
        _ request: CreateUserRequest
    ) async throws -> User {
        _ = try await UserService.shared.createUser(request)

        return try await login(
            email: request.email,
            password: request.password
        )
    }

    func logout() {
        api.clearTokens()
    }
}
