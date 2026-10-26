// Services/Network/APIClient.swift

import Foundation

final class APIClient {
    static let shared = APIClient()

    private let baseURL = "http://localhost:8080"

    private init() {}

    private var accessToken: String? {
        get {
            UserDefaults.standard.string(
                forKey: "betwise_access_token"
            )
        }

        set {
            UserDefaults.standard.set(
                newValue,
                forKey: "betwise_access_token"
            )
        }
    }

    private var refreshToken: String? {
        get {
            UserDefaults.standard.string(
                forKey: "betwise_refresh_token"
            )
        }

        set {
            UserDefaults.standard.set(
                newValue,
                forKey: "betwise_refresh_token"
            )
        }
    }

    func saveTokens(
        accessToken: String?,
        refreshToken: String?
    ) {
        if let accessToken {
            self.accessToken = accessToken
        }

        if let refreshToken {
            self.refreshToken = refreshToken
        }
    }

    func getRefreshToken() -> String? {
        refreshToken
    }

    func clearTokens() {
        accessToken = nil
        refreshToken = nil
    }

    func request<Response: Decodable>(
        path: String,
        method: String,
        authenticated: Bool = true
    ) async throws -> Response {
        try await request(
            path: path,
            method: method,
            body: Optional<String>.none,
            authenticated: authenticated
        )
    }

    func request<Response: Decodable, Body: Encodable>(
        path: String,
        method: String,
        body: Body?,
        authenticated: Bool = true
    ) async throws -> Response {
        guard let url = URL(string: baseURL + path) else {
            throw APIError.invalidURL
        }

        var request = URLRequest(url: url)

        request.httpMethod = method

        request.setValue(
            "application/json",
            forHTTPHeaderField: "Accept"
        )

        if let body {
            request.httpBody = try JSONEncoder().encode(body)

            request.setValue(
                "application/json",
                forHTTPHeaderField: "Content-Type"
            )
        }

        if authenticated {
            guard let accessToken else {
                throw APIError.unauthorized
            }

            request.setValue(
                "Bearer \(accessToken)",
                forHTTPHeaderField: "Authorization"
            )
        }

        let (data, response) = try await URLSession.shared.data(
            for: request
        )

        guard let httpResponse = response as? HTTPURLResponse else {
            throw APIError.invalidResponse
        }

        guard 200..<300 ~= httpResponse.statusCode else {
            if httpResponse.statusCode == 401 {
                throw APIError.unauthorized
            }

            throw APIError.server(
                httpResponse.statusCode
            )
        }

        if Response.self == EmptyResponse.self && data.isEmpty {
            return EmptyResponse() as! Response
        }

        do {
            return try JSONDecoder().decode(
                Response.self,
                from: data
            )
        } catch {
            throw APIError.decoding(error)
        }
    }
}
