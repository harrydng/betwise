// Core/SampleData/PriceHistory.swift

import Foundation

/// The backend has no price-history endpoint yet, so charts use generated
/// paths. They're seeded by ticker and range, so each chart looks the same
/// every time and always ends at the real current price.
enum PriceHistory {
    static func series(
        seed: String,
        range: ChartRange,
        endValue: Double,
        changePercent: Double
    ) -> [PricePoint] {
        var generator = SeededGenerator(seed: stableHash("\(seed)-\(range.rawValue)"))
        let count = range.pointCount
        let startValue = endValue / (1 + changePercent / 100)
        var noise = 0.0
        var velocity = 0.0

        return (0..<count).map { index in
            let progress = Double(index) / Double(count - 1)

            // A random walk with momentum, pulled gently back toward the
            // trend line. Momentum gives smooth swings instead of jitter.
            velocity = velocity * 0.82 + Double.random(in: -1...1, using: &generator) * range.volatility * 0.4
            noise = noise * 0.95 + velocity

            // Fade the wiggle out at both ends so the path starts and ends
            // exactly on the expected values.
            let envelope = sin(progress * .pi)
            let trend = startValue + (endValue - startValue) * progress

            return PricePoint(id: index, value: trend * (1 + noise * envelope))
        }
    }

    /// How far `seed` moved over `range`, scaled from its daily move.
    static func changePercent(
        seed: String,
        range: ChartRange,
        dayChange: Double
    ) -> Double {
        guard range != .day else { return dayChange }

        var generator = SeededGenerator(seed: stableHash("\(seed)-change-\(range.rawValue)"))
        let scale = range.changeScale
        let change = dayChange * scale * 0.6 + Double.random(in: -1...1.6, using: &generator) * scale

        return max(change, -60)
    }
}
