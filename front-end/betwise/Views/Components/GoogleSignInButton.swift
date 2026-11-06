import AuthenticationServices
import SwiftUI

/// "Continue with Google". Works for both new and returning users: the
/// backend creates the account the first time.
struct GoogleSignInButton: View {
    let session: SessionModel
    /// Reports failures to the hosting screen. Cancelling isn't a failure.
    let onError: (String) -> Void

    @Environment(\.webAuthenticationSession) private var webAuthenticationSession

    @State private var isWorking = false

    var body: some View {
        Button {
            Task {
                await signIn()
            }
        } label: {
            HStack(spacing: 10) {
                if isWorking {
                    ProgressView()
                        .tint(.black)
                } else {
                    GoogleMark()
                        .frame(width: 18, height: 18)
                        .accessibilityHidden(true)

                    Text("Continue with Google")
                }
            }
            .font(.headline)
            .foregroundStyle(Color(white: 0.12))
            .frame(maxWidth: .infinity, minHeight: 50)
            .background(.white, in: Capsule())
        }
        .buttonStyle(.plain)
        .disabled(isWorking)
        .accessibilityLabel("Continue with Google")
    }

    private func signIn() async {
        isWorking = true
        defer { isWorking = false }

        do {
            try await session.signInWithGoogle { url, scheme in
                // Ephemeral: don't reuse the user's Safari Google session, so
                // they choose which account to use every time.
                try await webAuthenticationSession.authenticate(
                    using: url,
                    callbackURLScheme: scheme,
                    preferredBrowserSession: .ephemeral
                )
            }
        } catch GoogleSignInError.cancelled {
            // The user closed the sheet; nothing to report.
        } catch {
            onError(error.localizedDescription)
        }
    }
}

/// A simple four-color "G". Replace with Google's official logo asset from
/// https://developers.google.com/identity/branding-guidelines before release;
/// Google's guidelines require the official mark.
private struct GoogleMark: View {
    var body: some View {
        Text("G")
            .font(.system(size: 17, weight: .bold, design: .rounded))
            .foregroundStyle(
                AngularGradient(
                    colors: [
                        Color(red: 0.92, green: 0.26, blue: 0.21),
                        Color(red: 0.98, green: 0.74, blue: 0.02),
                        Color(red: 0.20, green: 0.66, blue: 0.33),
                        Color(red: 0.26, green: 0.52, blue: 0.96),
                        Color(red: 0.92, green: 0.26, blue: 0.21)
                    ],
                    center: .center
                )
            )
    }
}

/// A thin "or" divider between sign-in options.
struct OrDivider: View {
    var body: some View {
        HStack(spacing: 12) {
            Rectangle().fill(AppColors.stroke).frame(height: 1)
            Text("or")
                .font(.footnote)
                .foregroundStyle(AppColors.textTertiary)
            Rectangle().fill(AppColors.stroke).frame(height: 1)
        }
        .accessibilityHidden(true)
    }
}
