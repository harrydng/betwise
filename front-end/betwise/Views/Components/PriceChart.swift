import SwiftUI
import Charts

/// The main interactive chart. Drag across it to scrub; the parent reads
/// `selectedIndex` to show the value under the finger.
struct PriceChart: View {
    let points: [PricePoint]
    let tint: Color
    @Binding var selectedIndex: Int?

    var body: some View {
        let domain = yDomain

        Chart {
            ForEach(points) { point in
                AreaMark(
                    x: .value("Time", point.id),
                    yStart: .value("Baseline", domain.lowerBound),
                    yEnd: .value("Price", point.value)
                )
                .interpolationMethod(.catmullRom)
                .foregroundStyle(
                    LinearGradient(
                        colors: [tint.opacity(0.32), tint.opacity(0)],
                        startPoint: .top,
                        endPoint: .bottom
                    )
                )

                LineMark(
                    x: .value("Time", point.id),
                    y: .value("Price", point.value)
                )
                .interpolationMethod(.catmullRom)
                .lineStyle(StrokeStyle(lineWidth: 2.2, lineCap: .round, lineJoin: .round))
                .foregroundStyle(tint)
            }

            if let selectedIndex, points.indices.contains(selectedIndex) {
                RuleMark(x: .value("Selected", selectedIndex))
                    .lineStyle(StrokeStyle(lineWidth: 1, dash: [3, 3]))
                    .foregroundStyle(.white.opacity(0.4))

                PointMark(
                    x: .value("Selected", selectedIndex),
                    y: .value("Price", points[selectedIndex].value)
                )
                .symbolSize(110)
                .foregroundStyle(tint)
            }
        }
        .chartXAxis(.hidden)
        .chartYAxis(.hidden)
        .chartLegend(.hidden)
        .chartYScale(domain: domain)
        .chartXSelection(value: $selectedIndex)
        .sensoryFeedback(.selection, trigger: selectedIndex)
        .accessibilityLabel("Price chart")
    }

    private var yDomain: ClosedRange<Double> {
        let values = points.map(\.value)

        guard let low = values.min(), let high = values.max(), high > low else {
            let value = points.first?.value ?? 0
            return (value - 1)...(value + 1)
        }

        let padding = (high - low) * 0.12
        return (low - padding)...(high + padding)
    }
}
