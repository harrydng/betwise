import SwiftUI

/// The first screen a signed-out user sees.
struct WelcomeView: View {
    let session: SessionModel

    @State private var presentedSheet: AuthSheet?

    var body: some View {
        VStack(spacing: 0) {
            Spacer()

            BrandHeader()

            Spacer()

            WelcomeHighlights()
                .padding(.bottom, 36)

            VStack(spacing: 12) {
                PrimaryButton(title: "Create account") {
                    presentedSheet = .signUp
                }

                Button {
                    presentedSheet = .signIn
                } label: {
                    Text("Sign in")
                        .font(.headline)
                        .frame(maxWidth: .infinity)
                }
                .buttonStyle(.glass)
                .controlSize(.large)
                .buttonBorderShape(.capsule)

                Button("Explore with sample data") {
                    session.continueAsGuest()
                }
                .font(.subheadline.weight(.semibold))
                .foregroundStyle(AppColors.accentBright)
                .padding(.top, 8)
            }
        }
        .padding(.horizontal, 24)
        .padding(.bottom, 16)
        .frame(maxWidth: 520)
        .frame(maxWidth: .infinity)
        .background {
            WelcomeBackground()
        }
        .sheet(item: $presentedSheet) { sheet in
            switch sheet {
            case .signIn:
                SignInView(session: session)
            case .signUp:
                SignUpView(session: session)
            }
        }
    }
}

private enum AuthSheet: Identifiable {
    case signIn
    case signUp

    var id: Self { self }
}

private struct BrandHeader: View {
    var body: some View {
        VStack(spacing: 20) {
            Image(systemName: "chart.line.uptrend.xyaxis")
                .font(.system(size: 44, weight: .bold))
                .foregroundStyle(.white)
                .frame(width: 96, height: 96)
                .background(
                    LinearGradient(
                        colors: [AppColors.accentBright, AppColors.accent, AppColors.accentDeep],
                        startPoint: .topLeading,
                        endPoint: .bottomTrailing
                    ),
                    in: .rect(cornerRadius: 28, style: .continuous)
                )
                .shadow(color: AppColors.accent.opacity(0.5), radius: 30, y: 10)
                .accessibilityHidden(true)

            VStack(spacing: 10) {
                Text("Betwise")
                    .font(.system(.largeTitle, design: .rounded, weight: .bold))
                    .foregroundStyle(AppColors.textPrimary)

                Text("Learn to invest with practice money.\nNo risk, real skills.")
                    .font(.title3)
                    .foregroundStyle(AppColors.textSecondary)
                    .multilineTextAlignment(.center)
            }
        }
    }
}

private struct WelcomeHighlights: View {
    var body: some View {
        VStack(alignment: .leading, spacing: 18) {
            Highlight(
                symbol: "chart.pie.fill",
                title: "Build a practice portfolio",
                detail: "Trade real stocks and ETFs at live prices."
            )
            Highlight(
                symbol: "graduationcap.fill",
                title: "Learn as you go",
                detail: "Short lessons that pay out buying power."
            )
            Highlight(
                symbol: "bell.badge.fill",
                title: "Stay in the loop",
                detail: "Get notified when orders fill and rewards land."
            )
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }
}

private struct Highlight: View {
    let symbol: String
    let title: LocalizedStringKey
    let detail: LocalizedStringKey

    var body: some View {
        HStack(spacing: 14) {
            Image(systemName: symbol)
                .font(.title3)
                .foregroundStyle(AppColors.accentBright)
                .frame(width: 44, height: 44)
                .background(AppColors.accent.opacity(0.16), in: .rect(cornerRadius: 13, style: .continuous))
                .accessibilityHidden(true)

            VStack(alignment: .leading, spacing: 2) {
                Text(title)
                    .font(.subheadline.weight(.semibold))
                    .foregroundStyle(AppColors.textPrimary)

                Text(detail)
                    .font(.footnote)
                    .foregroundStyle(AppColors.textSecondary)
            }
        }
        .accessibilityElement(children: .combine)
    }
}

/// A soft blue glow behind the logo on an otherwise black screen.
private struct WelcomeBackground: View {
    var body: some View {
        ZStack {
            AppColors.background

            RadialGradient(
                colors: [AppColors.accent.opacity(0.35), .clear],
                center: .init(x: 0.5, y: 0.22),
                startRadius: 10,
                endRadius: 380
            )
        }
        .ignoresSafeArea()
    }
}

#Preview {
    WelcomeView(session: SessionModel())
        .preferredColorScheme(.dark)
}
