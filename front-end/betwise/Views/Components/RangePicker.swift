import SwiftUI

/// The 1D / 1W / 1M … selector under a chart.
struct RangePicker: View {
    @Binding var selection: ChartRange

    @Namespace private var namespace

    var body: some View {
        HStack(spacing: 4) {
            ForEach(ChartRange.allCases) { range in
                let isSelected = range == selection

                Button {
                    withAnimation(.snappy) {
                        selection = range
                    }
                } label: {
                    Text(range.rawValue)
                        .font(.footnote.weight(.semibold))
                        .monospacedDigit()
                        .foregroundStyle(isSelected ? AppColors.accentBright : AppColors.textSecondary)
                        .frame(maxWidth: .infinity, minHeight: 34)
                        .background {
                            if isSelected {
                                Capsule()
                                    .fill(AppColors.accent.opacity(0.18))
                                    .matchedGeometryEffect(id: "selectedRange", in: namespace)
                            }
                        }
                        .contentShape(Capsule())
                }
                .buttonStyle(.plain)
                .accessibilityLabel(range.accessibilityName)
                .accessibilityAddTraits(isSelected ? .isSelected : [])
            }
        }
    }
}
