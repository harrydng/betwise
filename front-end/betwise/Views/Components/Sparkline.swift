import SwiftUI
import Charts

/// A tiny, axis-free line chart for list rows and cards.
struct Sparkline: View {
    let points: [PricePoint]
    let isGain: Bool

    var body: some View {
        let color = isGain ? AppColors.gain : AppColors.loss

        Chart(points) { point in
            LineMark(
                x: .value("Time", point.id),
                y: .value("Price", point.value)
            )
            .interpolationMethod(.catmullRom)
            .lineStyle(StrokeStyle(lineWidth: 1.6, lineCap: .round, lineJoin: .round))
            .foregroundStyle(color)
        }
        .chartXAxis(.hidden)
        .chartYAxis(.hidden)
        .chartYScale(domain: .automatic(includesZero: false))
        .chartLegend(.hidden)
        .accessibilityHidden(true)
    }
}
