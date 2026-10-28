import SwiftUI

struct FilterOption<Value: Hashable>: Hashable {
    let value: Value
    let title: String
}

/// A horizontally scrolling row of pill-shaped filters. It bleeds to the
/// screen edges so chips scroll under the margins instead of being cut off.
struct FilterChips<Value: Hashable>: View {
    let options: [FilterOption<Value>]
    @Binding var selection: Value

    var body: some View {
        ScrollView(.horizontal) {
            HStack(spacing: 8) {
                ForEach(options, id: \.value) { option in
                    let isSelected = option.value == selection

                    Button {
                        withAnimation(.snappy) {
                            selection = option.value
                        }
                    } label: {
                        Text(option.title)
                            .font(.subheadline.weight(.semibold))
                            .foregroundStyle(isSelected ? Color.white : AppColors.textSecondary)
                            .padding(.horizontal, 16)
                            .padding(.vertical, 9)
                            .background(isSelected ? AppColors.accent : AppColors.surface, in: Capsule())
                            .overlay {
                                Capsule().strokeBorder(isSelected ? Color.clear : AppColors.stroke)
                            }
                    }
                    .buttonStyle(.plain)
                    .accessibilityAddTraits(isSelected ? .isSelected : [])
                }
            }
        }
        .scrollIndicators(.hidden)
        .contentMargins(.horizontal, 20, for: .scrollContent)
        .padding(.horizontal, -20)
    }
}
