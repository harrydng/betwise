import SwiftUI

enum AppTab: Hashable {
    case portfolio
    case market
    case learn
    case news
    case search
}

struct ContentView: View {
    @State private var session = SessionModel()

    var body: some View {
        Group {
            switch session.state {
            case .signedOut:
                WelcomeView(session: session)
                    .transition(.opacity)
            case .guest, .signedIn:
                MainTabView(session: session)
                    // A fresh set of models per session, so a guest's
                    // sample data never leaks into a real account.
                    .id(session.state)
                    .transition(.opacity)
            }
        }
        .animation(.smooth, value: session.state)
        .preferredColorScheme(.dark)
        .task {
            await session.loadUser()
        }
    }
}

struct MainTabView: View {
    let session: SessionModel

    @State private var selectedTab: AppTab = .portfolio
    @State private var portfolioModel: PortfolioModel
    @State private var marketModel: MarketModel
    @State private var lessonsModel: LessonsModel
    @State private var notificationsModel: NotificationsModel
    @State private var newsModel: NewsModel

    init(session: SessionModel) {
        let usesSampleData = session.state == .guest

        self.session = session
        self.portfolioModel = PortfolioModel(usesSampleData: usesSampleData)
        self.marketModel = MarketModel(usesSampleData: usesSampleData)
        self.lessonsModel = LessonsModel(usesSampleData: usesSampleData)
        self.notificationsModel = NotificationsModel(usesSampleData: usesSampleData)
        self.newsModel = NewsModel(usesSampleData: usesSampleData)
    }

    var body: some View {
        TabView(selection: $selectedTab) {
            Tab("Portfolio", systemImage: "chart.pie", value: AppTab.portfolio) {
                NavigationStack {
                    PortfolioView(
                        model: portfolioModel,
                        market: marketModel,
                        session: session,
                        notifications: notificationsModel,
                        potentialLessonRewards: lessonsModel.remainingRewards
                    ) {
                        selectedTab = .learn
                    }
                }
            }

            Tab("Market", systemImage: "chart.line.uptrend.xyaxis", value: AppTab.market) {
                NavigationStack {
                    MarketView(model: marketModel, portfolio: portfolioModel)
                }
            }

            Tab("Learn", systemImage: "graduationcap", value: AppTab.learn) {
                NavigationStack {
                    LessonsView(model: lessonsModel, portfolio: portfolioModel)
                }
            }

            Tab("News", systemImage: "newspaper", value: AppTab.news) {
                NavigationStack {
                    NewsView(model: newsModel)
                }
            }

            Tab(value: AppTab.search, role: .search) {
                NavigationStack {
                    SearchView(model: marketModel, portfolio: portfolioModel)
                }
            }
        }
        .tint(AppColors.accent)
        #if os(iOS)
        .tabBarMinimizeBehavior(.onScrollDown)
        #endif
    }
}

#Preview("Signed out") {
    ContentView()
}

#Preview("Guest") {
    let session = SessionModel()
    session.continueAsGuest()

    return MainTabView(session: session)
        .preferredColorScheme(.dark)
}
