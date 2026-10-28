import SwiftUI

/// A tinted pill showing a percentage move, such as "▲ 2.45%".
struct ChangeBadge: View {
    /// The move in percent, where 2.5 means +2.5%.
    let percent: Double

    var body: some View {
        let color = AppColors.movement(percent)

        HStack(spacing: 3) {
            Image(systemName: percent >= 0 ? "arrowtriangle.up.fill" : "arrowtriangle.down.fill")
                .imageScale(.small)
                .font(.caption2)

            Text(abs(percent) / 100, format: .percent.precision(.fractionLength(2)))
                .monospacedDigit()
        }
        .font(.caption.weight(.semibold))
        .foregroundStyle(color)
        .padding(.horizontal, 8)
        .padding(.vertical, 4)
        .background(color.opacity(0.14), in: Capsule())
        .accessibilityElement(children: .ignore)
        .accessibilityLabel(
            percent >= 0
                ? Text("Up \(abs(percent) / 100, format: .percent.precision(.fractionLength(2)))")
                : Text("Down \(abs(percent) / 100, format: .percent.precision(.fractionLength(2)))")
        )
    }
}

#Preview {
    HStack {
        ChangeBadge(percent: 2.45)
        ChangeBadge(percent: -1.1)
    }
    .padding()
    .background(.black)
}
