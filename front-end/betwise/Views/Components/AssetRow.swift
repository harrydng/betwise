import SwiftUI

/// One asset in a list: logo, name, today's sparkline, price and move.
struct AssetRow: View {
    let ticker: String
    let name: String
    let price: Decimal
    let changePercent: Double
    let sparkline: [PricePoint]

    @Environment(\.dynamicTypeSize) private var dynamicTypeSize

    var body: some View {
        HStack(spacing: 14) {
            TickerLogo(ticker: ticker)

            VStack(alignment: .leading, spacing: 3) {
                Text(ticker)
                    .font(.headline)
                    .foregroundStyle(AppColors.textPrimary)

                Text(name)
                    .font(.subheadline)
                    .foregroundStyle(AppColors.textSecondary)
                    .lineLimit(1)
            }

            Spacer(minLength: 8)

            // There's no room for the sparkline at accessibility text sizes.
            if !dynamicTypeSize.isAccessibilitySize {
                Sparkline(points: sparkline, isGain: changePercent >= 0)
                    .frame(width: 60, height: 28)
            }

            VStack(alignment: .trailing, spacing: 4) {
                Text(price, format: .currency(code: "USD"))
                    .font(.headline)
                    .monospacedDigit()
                    .foregroundStyle(AppColors.textPrimary)

                ChangeBadge(percent: changePercent)
            }
        }
        .padding(.vertical, 12)
        .contentShape(.rect)
        .accessibilityElement(children: .combine)
    }
}
