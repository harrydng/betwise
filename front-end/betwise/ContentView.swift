import SwiftUI

struct ContentView: View {
    @StateObject private var viewModel = AppViewModel()

    var body: some View {
        ZStack {
            AppBackground()

            VStack(spacing: 0) {
                TopBar()

                Group {
                    switch viewModel.selectedTab {
                    case .news:
                        NewsView()
                    case .portfolio:
                        PortfolioView()
                    case .market:
                        MarketView()
                    case .lessons:
                        LessonsView()
                    }
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
            }

            VStack {
                Spacer()
                FloatingTabBar(
                    selectedTab: $viewModel.selectedTab
                )
                .padding(.bottom, 8)
            }
        }
    }
}

#Preview {
    ContentView()
}
