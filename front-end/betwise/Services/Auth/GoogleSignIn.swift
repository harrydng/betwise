// Services/Auth/GoogleSignIn.swift

import CryptoKit
import Foundation
import Security

/// Where to find the app's Google OAuth client.
///
/// Create an **iOS** OAuth client in Google Cloud Console (APIs & Services →
/// Credentials → Create credentials → OAuth client ID → iOS), using this
/// app's bundle identifier, then paste its client ID here. Add the same ID
/// to the backend's `betwise.google.client-ids`.
///
/// Client IDs aren't secrets; every iOS app ships its own.
enum GoogleSignInConfiguration {
    static let clientID = ""

    static var isConfigured: Bool {
        !clientID.isEmpty
    }

    /// Google's iOS clients redirect to the client ID reversed, for example
    /// `com.googleusercontent.apps.1234-abcd`.
    static var redirectScheme: String {
        clientID.split(separator: ".").reversed().joined(separator: ".")
    }

    static var redirectURI: String {
        "\(redirectScheme):/oauthredirect"
    }
}

/// The result of a Google sign-in, ready to send to the backend.
struct GoogleAuthorization {
    let idToken: String

    /// Must be sent with the ID token. The backend checks it matches the
    /// token's `nonce` claim, which ties the token to this sign-in.
    let nonce: String
}

enum GoogleSignInError: LocalizedError {
    case notConfigured
    case cancelled
    case invalidResponse
    case google(String)

    var errorDescription: String? {
        switch self {
        case .notConfigured:
            String(localized: "Google sign-in isn't set up yet. Add a client ID in GoogleSignInConfiguration.")
        case .cancelled:
            String(localized: "Google sign-in was cancelled.")
        case .invalidResponse:
            String(localized: "Google sign-in didn't complete. Please try again.")
        case .google(let message):
            message
        }
    }
}

/// Signs in with Google using the OAuth 2.0 authorization code flow with
/// PKCE, the flow Google recommends for native apps:
/// https://developers.google.com/identity/protocols/oauth2/native-app
///
/// 1. Open Google's sign-in page in a system web authentication sheet. The
///    app never sees the user's Google password.
/// 2. Google redirects back with a one-time code.
/// 3. Exchange the code, plus the PKCE secret only this app knows, for an ID
///    token. A stolen code is useless without that secret.
enum GoogleSignIn {
    /// Opens `url` in a web authentication session and returns the URL
    /// Google redirects to. Views pass SwiftUI's `webAuthenticationSession`.
    typealias Authenticate = (_ url: URL, _ callbackScheme: String) async throws -> URL

    private static let authorizationEndpoint = URL(string: "https://accounts.google.com/o/oauth2/v2/auth")
    private static let tokenEndpoint = URL(string: "https://oauth2.googleapis.com/token")

    static func authorize(using authenticate: Authenticate) async throws -> GoogleAuthorization {
        guard GoogleSignInConfiguration.isConfigured,
              let authorizationEndpoint,
              let tokenEndpoint else {
            throw GoogleSignInError.notConfigured
        }

        let verifier = randomURLSafeString()
        let state = randomURLSafeString()
        let nonce = randomURLSafeString()

        var components = URLComponents(url: authorizationEndpoint, resolvingAgainstBaseURL: false)
        components?.queryItems = [
            URLQueryItem(name: "client_id", value: GoogleSignInConfiguration.clientID),
            URLQueryItem(name: "redirect_uri", value: GoogleSignInConfiguration.redirectURI),
            URLQueryItem(name: "response_type", value: "code"),
            URLQueryItem(name: "scope", value: "openid email profile"),
            URLQueryItem(name: "code_challenge", value: codeChallenge(for: verifier)),
            URLQueryItem(name: "code_challenge_method", value: "S256"),
            URLQueryItem(name: "state", value: state),
            URLQueryItem(name: "nonce", value: nonce),
            URLQueryItem(name: "prompt", value: "select_account")
        ]

        guard let url = components?.url else {
            throw GoogleSignInError.notConfigured
        }

        let callback: URL

        do {
            callback = try await authenticate(url, GoogleSignInConfiguration.redirectScheme)
        } catch {
            throw GoogleSignInError.cancelled
        }

        let code = try authorizationCode(from: callback, expectedState: state)

        let idToken = try await exchange(
            code: code,
            verifier: verifier,
            at: tokenEndpoint
        )

        return GoogleAuthorization(idToken: idToken, nonce: nonce)
    }

    /// Pulls the code out of Google's redirect, rejecting it unless `state`
    /// matches. That stops another app or page from feeding in a code from
    /// a sign-in this app didn't start.
    static func authorizationCode(from callback: URL, expectedState: String) throws -> String {
        let items = URLComponents(url: callback, resolvingAgainstBaseURL: false)?.queryItems ?? []
        let value = { (name: String) in items.first { $0.name == name }?.value }

        if value("error") == "access_denied" {
            throw GoogleSignInError.cancelled
        }

        if let error = value("error") {
            throw GoogleSignInError.google(String(localized: "Google sign-in failed (\(error))."))
        }

        guard value("state") == expectedState else {
            throw GoogleSignInError.invalidResponse
        }

        guard let code = value("code"), !code.isEmpty else {
            throw GoogleSignInError.invalidResponse
        }

        return code
    }

    private struct TokenResponse: Decodable {
        let idToken: String?
        let error: String?
        let errorDescription: String?

        enum CodingKeys: String, CodingKey {
            case idToken = "id_token"
            case error
            case errorDescription = "error_description"
        }
    }

    private static func exchange(code: String, verifier: String, at endpoint: URL) async throws -> String {
        var request = URLRequest(url: endpoint)
        request.httpMethod = "POST"
        request.setValue("application/x-www-form-urlencoded", forHTTPHeaderField: "Content-Type")

        var form = URLComponents()
        form.queryItems = [
            URLQueryItem(name: "code", value: code),
            URLQueryItem(name: "client_id", value: GoogleSignInConfiguration.clientID),
            URLQueryItem(name: "code_verifier", value: verifier),
            URLQueryItem(name: "redirect_uri", value: GoogleSignInConfiguration.redirectURI),
            URLQueryItem(name: "grant_type", value: "authorization_code")
        ]
        request.httpBody = Data((form.percentEncodedQuery ?? "").utf8)

        let (data, _) = try await URLSession.shared.data(for: request)
        let response = try? JSONDecoder().decode(TokenResponse.self, from: data)

        guard let idToken = response?.idToken else {
            throw GoogleSignInError.google(
                response?.errorDescription ?? String(localized: "Google didn't return a sign-in token.")
            )
        }

        return idToken
    }

    // MARK: - PKCE helpers

    /// SHA-256 of the verifier, base64url-encoded (RFC 7636 `S256`).
    static func codeChallenge(for verifier: String) -> String {
        base64URL(Data(SHA256.hash(data: Data(verifier.utf8))))
    }

    /// 32 random bytes from the system's secure generator, base64url-encoded
    /// to 43 characters. Used for the PKCE verifier, state and nonce.
    static func randomURLSafeString() -> String {
        var bytes = [UInt8](repeating: 0, count: 32)
        let status = SecRandomCopyBytes(kSecRandomDefault, bytes.count, &bytes)

        if status != errSecSuccess {
            // SystemRandomNumberGenerator is also cryptographically secure.
            var generator = SystemRandomNumberGenerator()
            bytes = bytes.map { _ in UInt8.random(in: .min ... .max, using: &generator) }
        }

        return base64URL(Data(bytes))
    }

    private static func base64URL(_ data: Data) -> String {
        data.base64EncodedString()
            .replacingOccurrences(of: "+", with: "-")
            .replacingOccurrences(of: "/", with: "_")
            .replacingOccurrences(of: "=", with: "")
    }
}
