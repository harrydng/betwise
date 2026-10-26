import SwiftUI

struct FloatingTabBar: View {
    @Binding var selectedTab: AppTab

    var body: some View {
        HStack(spacing: 20) {
            ForEach(AppTab.allCases, id: \.self) { tab in
                // The buttons to
                Button {
                    withAnimation(
                        .spring(
                            response: 0.35,
                            dampingFraction: 0.78
                        )
                    ) {
                        selectedTab = tab
                    }
                } label: {
                    HStack(spacing: 10) {
                        Image(systemName: tab.icon)
                            .font(.system(size: 17, weight: .semibold))

                        if selectedTab == tab {
                            Text(tab.title)
                                .font(
                                    .system(
                                        size: 13,
                                        weight: .semibold
                                    )
                                )
                                .transition(
                                    .opacity.combined(
                                        with: .move(edge: .leading)
                                    )
                                )
                        }
                    }
                    .foregroundStyle(
                        selectedTab == tab
                        ? Color.white
                        : AppColors.textPrimary
                    )
                    .padding(.horizontal, selectedTab == tab ? 16 : 13)
                    .frame(height: 46)
                    .background {
                        if selectedTab == tab {
                            Capsule()
                                .fill(
                                    AppColors.primaryBlue.opacity(0.8)
                                )
                        }
                    }
                    .contentShape(Capsule())
                }
                .buttonStyle(.plain)
            }
        }
        .padding(7)
        .background(
            Color.black.opacity(0.9)
        )
        .background(.ultraThinMaterial)
        .clipShape(
            Capsule()
        )
        .shadow(
            color: Color.black.opacity(0.1),
            radius: 18,
            y: 8
        )
        .padding(.horizontal, 18)
    }
}

#Preview {
    ContentView()
}
