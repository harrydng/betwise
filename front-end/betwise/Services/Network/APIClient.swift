// Services/Network/APIClient.swift

import Foundation

enum HTTPMethod: String {
    case get = "GET"
    case post = "POST"
    case patch = "PATCH"
    case delete = "DELETE"
}

final class APIClient {
    static let shared = APIClient()

    private let baseURL = "http://localhost:8080"

    private let tokenStore = TokenStore.shared

    private let encoder = JSONEncoder.betwise
    private let decoder = JSONDecoder.betwise

    // Refresh this long before the access token actually expires, so a
    // request never leaves the device with a token that dies in flight.
    private let refreshLeeway: TimeInterval = 60

    // Shared so that concurrent requests that all find an expired token
    // only trigger a single call to /api/auth/refresh.
    private var refreshTask: Task<Void, Error>?

    private init() {}

    var isSignedIn: Bool {
        tokenStore.refreshToken != nil
    }

    func saveTokens(
        accessToken: String,
        refreshToken: String
    ) {
        tokenStore.accessToken = accessToken
        tokenStore.refreshToken = refreshToken
    }

    func clearTokens() {
        tokenStore.clear()
    }

    // MARK: - Requests that return a body

    func request<Response: Decodable>(
        _ method: HTTPMethod,
        _ path: String,
        query: [URLQueryItem] = [],
        authenticated: Bool = true
    ) async throws -> Response {
        let data = try await send(
            method,
            path,
            query: query,
            body: nil,
            authenticated: authenticated
        )

        return try decode(data)
    }

    func request<Response: Decodable, Body: Encodable>(
        _ method: HTTPMethod,
        _ path: String,
        body: Body,
        authenticated: Bool = true
    ) async throws -> Response {
        let data = try await send(
            method,
            path,
            body: try encoder.encode(body),
            authenticated: authenticated
        )

        return try decode(data)
    }

    // MARK: - Requests that return no body (204 No Content)

    func perform(
        _ method: HTTPMethod,
        _ path: String,
        authenticated: Bool = true
    ) async throws {
        _ = try await send(
            method,
            path,
            body: nil,
            authenticated: authenticated
        )
    }

    func perform<Body: Encodable>(
        _ method: HTTPMethod,
        _ path: String,
        body: Body,
        authenticated: Bool = true
    ) async throws {
        _ = try await send(
            method,
            path,
            body: try encoder.encode(body),
            authenticated: authenticated
        )
    }

    // MARK: - Core

    private func send(
        _ method: HTTPMethod,
        _ path: String,
        query: [URLQueryItem] = [],
        body: Data?,
        authenticated: Bool
    ) async throws -> Data {
        guard var components = URLComponents(string: baseURL + path) else {
            throw APIError.invalidURL
        }

        if !query.isEmpty {
            components.queryItems = query

            // URLComponents leaves "+" alone, but Spring decodes it as a
            // space, so a search for "S&P+500" would lose the plus.
            components.percentEncodedQuery = components.percentEncodedQuery?
                .replacingOccurrences(of: "+", with: "%2B")
        }

        guard let url = components.url else {
            throw APIError.invalidURL
        }

        var request = URLRequest(url: url)
        request.httpMethod = method.rawValue
        request.setValue("application/json", forHTTPHeaderField: "Accept")

        if let body {
            request.httpBody = body
            request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        }

        if authenticated {
            let accessToken = try await validAccessToken()
            request.setValue("Bearer \(accessToken)", forHTTPHeaderField: "Authorization")
        }

        let (data, response) = try await URLSession.shared.data(for: request)

        guard let httpResponse = response as? HTTPURLResponse else {
            throw APIError.invalidResponse
        }

        switch httpResponse.statusCode {
        case 200..<300:
            return data

        // The access token was refreshed before sending, so a 401 here
        // means the server no longer accepts this session at all (for
        // example, the account was deleted).
        case 401 where authenticated:
            tokenStore.clear()
            throw APIError.notSignedIn

        case 403:
            throw APIError.forbidden

        // Everything else, including a 401 from login, carries the
        // server's message, such as "Invalid email or password".
        default:
            let errorBody = try? decoder.decode(ServerErrorBody.self, from: data)

            throw APIError.server(
                statusCode: httpResponse.statusCode,
                message: errorBody?.message
            )
        }
    }

    private func decode<Response: Decodable>(_ data: Data) throws -> Response {
        do {
            return try decoder.decode(Response.self, from: data)
        } catch {
            throw APIError.decoding(error)
        }
    }

    // MARK: - Tokens

    /// Returns an access token that is good for at least `refreshLeeway`
    /// seconds, refreshing it first if needed.
    private func validAccessToken() async throws -> String {
        if let refreshTask {
            try await refreshTask.value
        }

        if let accessToken = tokenStore.accessToken,
           let expiresAt = TokenStore.expirationDate(ofJWT: accessToken),
           expiresAt.timeIntervalSinceNow > refreshLeeway {
            return accessToken
        }

        try await refreshAccessToken()

        guard let accessToken = tokenStore.accessToken else {
            throw APIError.notSignedIn
        }

        return accessToken
    }

    private func refreshAccessToken() async throws {
        if let refreshTask {
            return try await refreshTask.value
        }

        let task = Task {
            defer { refreshTask = nil }

            guard let refreshToken = tokenStore.refreshToken else {
                throw APIError.notSignedIn
            }

            if let expiresAt = TokenStore.expirationDate(ofJWT: refreshToken),
               expiresAt <= .now {
                tokenStore.clear()
                throw APIError.notSignedIn
            }

            do {
                let response: RefreshTokenResponse = try await request(
                    .post,
                    "/api/auth/refresh",
                    body: RefreshRequest(refreshToken: refreshToken),
                    authenticated: false
                )

                tokenStore.accessToken = response.accessToken
            } catch APIError.server(statusCode: 401, message: _) {
                // The refresh token was rejected, so the session is over.
                tokenStore.clear()
                throw APIError.notSignedIn
            }
        }

        refreshTask = task
        try await task.value
    }
}

/// The backend's error JSON (`ApiErrorResponse`).
private struct ServerErrorBody: Decodable {
    let status: Int?
    let error: String?
    let message: String?
}
