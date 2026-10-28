// Models/Market/ChartRange.swift

import Foundation

enum ChartRange: String, CaseIterable, Identifiable {
    case day = "1D"
    case week = "1W"
    case month = "1M"
    case threeMonths = "3M"
    case year = "1Y"
    case all = "ALL"

    var id: Self { self }

    var periodDescription: String {
        switch self {
        case .day: String(localized: "Today")
        case .week: String(localized: "Past week")
        case .month: String(localized: "Past month")
        case .threeMonths: String(localized: "Past 3 months")
        case .year: String(localized: "Past year")
        case .all: String(localized: "All time")
        }
    }

    var accessibilityName: String {
        switch self {
        case .day: String(localized: "1 day")
        case .week: String(localized: "1 week")
        case .month: String(localized: "1 month")
        case .threeMonths: String(localized: "3 months")
        case .year: String(localized: "1 year")
        case .all: String(localized: "All time")
        }
    }

    // MARK: Sample data shape

    var pointCount: Int {
        switch self {
        case .day: 78
        case .week: 84
        case .month: 90
        case .threeMonths: 92
        case .year: 104
        case .all: 120
        }
    }

    /// Per-step noise for generated price paths.
    var volatility: Double {
        switch self {
        case .day: 0.0035
        case .week: 0.005
        case .month: 0.007
        case .threeMonths: 0.01
        case .year: 0.014
        case .all: 0.02
        }
    }

    /// Roughly how much bigger a move over this range is than a daily move.
    var changeScale: Double {
        switch self {
        case .day: 1
        case .week: 1.6
        case .month: 3
        case .threeMonths: 5.5
        case .year: 12
        case .all: 28
        }
    }
}
