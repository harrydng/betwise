import SwiftUI

/// The big number above a chart, plus its change over the range. While
/// the chart is being scrubbed it shows the value under the finger.
struct ChartValueHeader: View {
    let title: LocalizedStringKey
    let currentValue: Decimal
    let points: [PricePoint]
    let selectedIndex: Int?
    let range: ChartRange

    var body: some View {
        let start = points.first?.value ?? currentValue.doubleValue
        let scrubbed = selectedIndex.flatMap { points.indices.contains($0) ? points[$0].value : nil }
        let shown = scrubbed.map { Decimal($0) } ?? currentValue
        let changeAmount = shown - Decimal(start)
        let changePercent = start == 0 ? 0 : (shown.doubleValue - start) / start

        VStack(alignment: .leading, spacing: 6) {
            Text(title)
                .font(.subheadline.weight(.medium))
                .foregroundStyle(AppColors.textSecondary)

            Text(shown, format: .currency(code: "USD"))
                .font(.system(.largeTitle, design: .rounded, weight: .bold))
                .monospacedDigit()
                .foregroundStyle(AppColors.textPrimary)
                .contentTransition(.numericText())
                .lineLimit(1)
                .minimumScaleFactor(0.6)

            HStack(spacing: 6) {
                Text(changeAmount, format: .currency(code: "USD").sign(strategy: .always()))
                Text("(\(changePercent, format: .percent.precision(.fractionLength(2))))")

                Text(scrubbed == nil ? range.periodDescription : String(localized: "Selected"))
                    .foregroundStyle(AppColors.textSecondary)
            }
            .font(.subheadline.weight(.semibold))
            .monospacedDigit()
            .foregroundStyle(AppColors.movement(changePercent))
        }
        .accessibilityElement(children: .combine)
    }
}
