import SwiftUI

/// A round monogram standing in for a company logo. The shade of blue is
/// derived from the ticker, so each asset always gets the same color.
struct TickerLogo: View {
    let ticker: String
    var size: CGFloat = 44

    var body: some View {
        let base = AppColors.paletteColor(for: ticker)

        Text(ticker.prefix(ticker.count > 3 ? 2 : ticker.count))
            .font(.system(size: size * (ticker.count > 2 ? 0.3 : 0.36), weight: .bold, design: .rounded))
            .foregroundStyle(.white)
            .lineLimit(1)
            .minimumScaleFactor(0.6)
            .frame(width: size, height: size)
            .background {
                Circle()
                    .fill(
                        LinearGradient(
                            colors: [base, base.opacity(0.55)],
                            startPoint: .topLeading,
                            endPoint: .bottomTrailing
                        )
                    )
            }
            .overlay {
                Circle().strokeBorder(.white.opacity(0.12), lineWidth: 1)
            }
            .accessibilityHidden(true)
    }
}

#Preview {
    HStack {
        TickerLogo(ticker: "AAPL")
        TickerLogo(ticker: "NVDA")
        TickerLogo(ticker: "O")
    }
    .padding()
    .background(.black)
}
