import Foundation

extension Asset.AssetType {
    var displayName: String {
        switch self {
        case .stock: String(localized: "Stock")
        case .etf: String(localized: "ETF")
        case .bond: String(localized: "Bond")
        case .reit: String(localized: "REIT")
        }
    }

    var pluralName: String {
        switch self {
        case .stock: String(localized: "Stocks")
        case .etf: String(localized: "ETFs")
        case .bond: String(localized: "Bonds")
        case .reit: String(localized: "REITs")
        }
    }
}

extension User.ExperienceLevel {
    var displayName: String {
        switch self {
        case .beginner: String(localized: "Beginner")
        case .intermediate: String(localized: "Intermediate")
        case .advanced: String(localized: "Advanced")
        }
    }
}

extension User.InvestmentGoal {
    var displayName: String {
        switch self {
        case .capitalPreservation: String(localized: "Protect my money")
        case .income: String(localized: "Earn income")
        case .longTermGrowth: String(localized: "Long-term growth")
        case .shortTermGrowth: String(localized: "Short-term growth")
        }
    }
}

extension User.RiskTolerance {
    var displayName: String {
        switch self {
        case .conservative: String(localized: "Conservative")
        case .balanced: String(localized: "Balanced")
        case .aggressive: String(localized: "Aggressive")
        }
    }
}

extension AppNotification.NotificationType {
    var symbolName: String {
        switch self {
        case .tradeFilled: "arrow.left.arrow.right"
        case .cashTransfer: "banknote"
        case .lessonReward: "gift.fill"
        case .quizCompleted: "checkmark.seal.fill"
        }
    }
}

extension Lesson.Difficulty {
    var displayName: String {
        switch self {
        case .beginner: String(localized: "Beginner")
        case .intermediate: String(localized: "Intermediate")
        case .advanced: String(localized: "Advanced")
        }
    }

    /// 1–3 filled bars, like a signal meter.
    var level: Int {
        switch self {
        case .beginner: 1
        case .intermediate: 2
        case .advanced: 3
        }
    }
}

extension Lesson.Topic {
    var symbolName: String {
        switch self {
        case .stocks: "chart.line.uptrend.xyaxis"
        case .bonds: "building.columns"
        case .etfs: "square.stack.3d.up"
        case .diversification: "chart.pie"
        case .risk: "gauge.with.dots.needle.67percent"
        case .shortSelling: "arrow.down.forward"
        case .fundamentalAnalysis: "doc.text.magnifyingglass"
        case .technicalAnalysis: "waveform.path.ecg"
        case .realEstate: "house"
        }
    }
}
