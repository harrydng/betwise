// Services/Network/APIError.swift

import Foundation

enum APIError: LocalizedError {
    case invalidURL
    case invalidResponse
    case notSignedIn
    case forbidden
    case server(statusCode: Int, message: String?)
    case decoding(Error)

    var errorDescription: String? {
        switch self {
        case .invalidURL:
            return "Invalid API URL."

        case .invalidResponse:
            return "Invalid response from the server."

        case .notSignedIn:
            return "You are not signed in."

        case .forbidden:
            return "You don't have permission to do that."

        case .server(let statusCode, let message):
            return message ?? "Server returned status code \(statusCode)."

        case .decoding(let error):
            return "Could not read server response: \(error.localizedDescription)"
        }
    }
}
