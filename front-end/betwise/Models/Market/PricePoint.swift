// Models/Market/PricePoint.swift

import Foundation

/// One sample on a price chart. `id` is the point's position in the series.
struct PricePoint: Identifiable, Hashable {
    let id: Int
    let value: Double
}
