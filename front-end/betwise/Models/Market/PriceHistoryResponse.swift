// Models/Market/PriceHistoryResponse.swift

import Foundation

/// A chart series from `/api/assets/{id}/history` or
/// `/api/portfolios/{id}/history`.
struct PriceHistoryResponse: Codable {
    let range: String
    let changePercent: Decimal
    let points: [Point]

    struct Point: Codable, Hashable {
        let timestamp: Date
        let value: Decimal
    }

    /// The series in the shape the chart views draw.
    var chartPoints: [PricePoint] {
        points.enumerated().map { index, point in
            PricePoint(id: index, value: point.value.doubleValue)
        }
    }
}
