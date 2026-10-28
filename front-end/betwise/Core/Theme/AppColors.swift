//
//  AppColors.swift
//  betwise
//
//  Created by Harry Duong on 9/16/26.
//

import SwiftUI

enum AppColors {

    // Surfaces
    static let background = Color.black

    static let surface = Color(
        red: 12 / 255,
        green: 14 / 255,
        blue: 19 / 255
    )

    static let surfaceRaised = Color(
        red: 22 / 255,
        green: 25 / 255,
        blue: 33 / 255
    )

    static let stroke = Color.white.opacity(0.07)

    // Brand
    static let accent = Color(
        red: 41 / 255,
        green: 121 / 255,
        blue: 255 / 255
    )

    static let accentBright = Color(
        red: 110 / 255,
        green: 168 / 255,
        blue: 255 / 255
    )

    static let accentDeep = Color(
        red: 8 / 255,
        green: 36 / 255,
        blue: 110 / 255
    )

    // Text
    static let textPrimary = Color.white
    static let textSecondary = Color(white: 0.62)
    static let textTertiary = Color(white: 0.48)

    // Market movement
    static let gain = Color(
        red: 48 / 255,
        green: 209 / 255,
        blue: 140 / 255
    )

    static let loss = Color(
        red: 255 / 255,
        green: 95 / 255,
        blue: 95 / 255
    )

    static func movement(_ value: Double) -> Color {
        value >= 0 ? gain : loss
    }

    // Shades of blue for allocation bars and ticker logos, so the data
    // stays on-theme instead of turning into a rainbow.
    static let chartPalette: [Color] = [
        accent,
        Color(red: 0 / 255, green: 194 / 255, blue: 255 / 255),
        Color(red: 98 / 255, green: 92 / 255, blue: 240 / 255),
        accentBright,
        Color(red: 22 / 255, green: 76 / 255, blue: 200 / 255),
        Color(red: 150 / 255, green: 206 / 255, blue: 255 / 255)
    ]

    static let cash = Color(white: 0.32)

    static func paletteColor(for key: String) -> Color {
        chartPalette[Int(stableHash(key) % UInt64(chartPalette.count))]
    }
}
