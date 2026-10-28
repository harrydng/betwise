// Services/Network/TokenStore.swift

import Foundation
import Security

/// Keeps the access and refresh tokens in the Keychain.
final class TokenStore {
    static let shared = TokenStore()

    private let service = "com.betwise.auth"

    private init() {
        // Earlier builds stored tokens in UserDefaults. Remove them so they
        // don't linger in plain text.
        UserDefaults.standard.removeObject(forKey: "betwise_access_token")
        UserDefaults.standard.removeObject(forKey: "betwise_refresh_token")
    }

    var accessToken: String? {
        get { read(account: "accessToken") }
        set { write(newValue, account: "accessToken") }
    }

    var refreshToken: String? {
        get { read(account: "refreshToken") }
        set { write(newValue, account: "refreshToken") }
    }

    func clear() {
        accessToken = nil
        refreshToken = nil
    }

    /// Reads the `exp` claim of a JWT. The signature isn't checked; that's
    /// the server's job. The app only uses this to refresh before expiry.
    static func expirationDate(ofJWT token: String) -> Date? {
        let parts = token.split(separator: ".")

        guard parts.count == 3 else { return nil }

        // JWTs use unpadded base64url, which Data(base64Encoded:) rejects.
        var payload = String(parts[1])
            .replacingOccurrences(of: "-", with: "+")
            .replacingOccurrences(of: "_", with: "/")
        payload += String(repeating: "=", count: (4 - payload.count % 4) % 4)

        guard let data = Data(base64Encoded: payload),
              let claims = try? JSONDecoder().decode(JWTClaims.self, from: data)
        else {
            return nil
        }

        return Date(timeIntervalSince1970: claims.exp)
    }

    // MARK: - Keychain

    private func baseQuery(account: String) -> [String: Any] {
        [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: service,
            kSecAttrAccount as String: account
        ]
    }

    private func read(account: String) -> String? {
        var query = baseQuery(account: account)
        query[kSecReturnData as String] = true
        query[kSecMatchLimit as String] = kSecMatchLimitOne

        var item: CFTypeRef?

        guard SecItemCopyMatching(query as CFDictionary, &item) == errSecSuccess,
              let data = item as? Data
        else {
            return nil
        }

        return String(data: data, encoding: .utf8)
    }

    private func write(_ value: String?, account: String) {
        let query = baseQuery(account: account)

        SecItemDelete(query as CFDictionary)

        guard let value else { return }

        var attributes = query
        attributes[kSecValueData as String] = Data(value.utf8)
        attributes[kSecAttrAccessible as String] = kSecAttrAccessibleAfterFirstUnlock

        SecItemAdd(attributes as CFDictionary, nil)
    }
}

private struct JWTClaims: Decodable {
    let exp: TimeInterval
}
