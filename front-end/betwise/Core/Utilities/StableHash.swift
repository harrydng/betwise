// Core/Utilities/StableHash.swift

import Foundation

/// A hash that's identical on every launch, unlike `Hasher`, which is
/// seeded randomly per process. Used to derive repeatable sample data and
/// colors from strings such as tickers.
func stableHash(_ string: String) -> UInt64 {
    // FNV-1a
    var hash: UInt64 = 0xcbf2_9ce4_8422_2325

    for byte in string.utf8 {
        hash ^= UInt64(byte)
        hash = hash &* 0x0000_0100_0000_01b3
    }

    return hash
}

/// A small deterministic random number generator (SplitMix64), so sample
/// charts look the same every time the app runs.
struct SeededGenerator: RandomNumberGenerator {
    private var state: UInt64

    init(seed: UInt64) {
        state = seed
    }

    mutating func next() -> UInt64 {
        state &+= 0x9e37_79b9_7f4a_7c15
        var z = state
        z = (z ^ (z >> 30)) &* 0xbf58_476d_1ce4_e5b9
        z = (z ^ (z >> 27)) &* 0x94d0_49bb_1331_11eb
        return z ^ (z >> 31)
    }
}
