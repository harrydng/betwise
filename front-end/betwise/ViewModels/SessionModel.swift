// ViewModels/SessionModel.swift

import Foundation
import Observation

/// Who's using the app: nobody yet, a guest browsing sample data, or a
/// signed-in user seeing their own data.
enum SessionState: Hashable {
    case signedOut
    case guest
    case signedIn
}

@Observable
final class SessionModel {
    private(set) var state: SessionState
    private(set) var user: User?

    init() {
        // Tokens survive relaunches in the Keychain, so a returning user
        // goes straight into the app.
        state = AuthService.shared.isSignedIn ? .signedIn : .signedOut
    }

    func signIn(email: String, password: String) async throws {
        user = try await AuthService.shared.login(email: email, password: password)
        state = .signedIn
    }

    func signUp(_ request: CreateUserRequest) async throws {
        user = try await AuthService.shared.signUp(request)
        state = .signedIn
    }

    func continueAsGuest() {
        state = .guest
    }

    /// Leaves guest mode or the signed-in session and returns to the
    /// welcome screen.
    func signOut() {
        AuthService.shared.logout()
        user = nil
        state = .signedOut
    }

    /// Loads the profile for a returning user. If the saved session has
    /// expired, this sends them back to the welcome screen.
    func loadUser() async {
        guard state == .signedIn else { return }

        do {
            user = try await UserService.shared.getCurrentUser()
        } catch APIError.notSignedIn {
            signOut()
        } catch {
            // Keep the session; a network blip shouldn't sign anyone out.
        }
    }

    func updateProfile(_ request: UpdateUserRequest) async throws {
        user = try await UserService.shared.updateCurrentUser(request)
    }

    func changePassword(current: String, new: String) async throws {
        try await UserService.shared.changePassword(currentPassword: current, newPassword: new)
    }

    func deleteAccount() async throws {
        try await UserService.shared.deleteCurrentUser()
        user = nil
        state = .signedOut
    }
}
