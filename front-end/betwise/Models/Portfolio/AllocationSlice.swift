// Models/Portfolio/AllocationSlice.swift

import Foundation

/// One segment of the portfolio allocation bar.
struct AllocationSlice: Identifiable, Hashable {
    /// The ticker, or "cash".
    let id: String
    let label: String
    let fraction: Double
    let isCash: Bool
}
