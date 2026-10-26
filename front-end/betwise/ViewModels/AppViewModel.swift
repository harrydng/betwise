import Foundation
import Combine

enum AppTab: CaseIterable {
    case news
    case portfolio
    case market
    case lessons

    var icon: String {
        switch self {
        case .news:
            return "newspaper.fill"
        case .portfolio:
            return "chart.pie.fill"
        case .market:
            return "chart.line.uptrend.xyaxis"
        case .lessons:
            return "book.fill"
        }
    }

    var title: String {
        switch self {
        case .news:
            return "News"
        case .portfolio:
            return "Portfolio"
        case .market:
            return "Market"
        case .lessons:
            return "Learn"
        }
    }
}

final class AppViewModel: ObservableObject {
    @Published var selectedTab: AppTab = .market
}
