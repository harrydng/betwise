// Services/Auth/AuthService.swift

import Foundation

final class AuthService {
    static let shared = AuthService()

    private let api = APIClient.shared

    private init() {}

    func login(
        email: String,
        password: String
    ) async throws -> LoginResponse {
        let response: LoginResponse = try await api.request(
            path: "/api/auth/login",
            method: "POST",
            body: LoginRequest(
                email: email,
                password: password
            ),
            authenticated: false
        )

        api.saveTokens(
            accessToken: response.bearerToken,
            refreshToken: response.refreshToken
        )

        return response
    }

    func refreshAccessToken() async throws -> LoginResponse {
        guard let refreshToken = api.getRefreshToken() else {
            throw APIError.unauthorized
        }

        let response: LoginResponse = try await api.request(
            path: "/api/auth/refresh",
            method: "POST",
            body: RefreshRequest(
                refreshToken: refreshToken
            ),
            authenticated: false
        )

        api.saveTokens(
            accessToken: response.bearerToken,
            refreshToken: response.refreshToken
        )

        return response
    }

    func logout() {
        api.clearTokens()
    }
}
