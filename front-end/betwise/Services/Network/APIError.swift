// Services/Network/APIError.swift

import Foundation

enum APIError: LocalizedError {
    case invalidURL
    case invalidResponse
    case unauthorized
    case server(Int)
    case decoding(Error)

    var errorDescription: String? {
        switch self {
        case .invalidURL:
            return "Invalid API URL."

        case .invalidResponse:
            return "Invalid response from the server."

        case .unauthorized:
            return "You are not signed in."

        case .server(let code):
            return "Server returned status code \(code)."

        case .decoding(let error):
            return "Could not read server response: \(error.localizedDescription)"
        }
    }
}
