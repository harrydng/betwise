import SwiftUI

struct AppBackground: View {
    var body: some View {
        ZStack {
            
            
            // Dark navy / almost black base
            Color(
                red: 2 / 255,
                green: 8 / 255,
                blue: 18 / 255
            )

            // Dark top -> dark blue bottom
            LinearGradient(
                stops: [
                    .init(
                        color: Color.blue.opacity(0.0),
                        location: 0.0
                    ),
                    .init(
                        color: Color(
                            red: 0 / 255,
                            green: 45 / 255,
                            blue: 120 / 255
                        ).opacity(0.25),
                        location: 0.35
                    ),
                    .init(
                        color: Color(
                            red: 0 / 255,
                            green: 75 / 255,
                            blue: 200 / 255
                        ).opacity(0.65),
                        location: 1.0
                    )
                ],
                startPoint: .top,
                endPoint: .bottom
            )
            .frame(maxWidth: .infinity)
            .frame(height: 400)
            .frame(maxHeight: .infinity, alignment: .bottom)

            // Blue glow rising from bottom
            RadialGradient(
                colors: [
                    Color(
                        red: 0 / 255,
                        green: 85 / 255,
                        blue: 220 / 255
                    )
                    .opacity(0.65),

                    Color(
                        red: 0 / 255,
                        green: 55 / 255,
                        blue: 150 / 255
                    )
                    .opacity(0.30),

                    Color.clear
                ],
                center: UnitPoint(
                    x: 0.5,
                    y: 1.12
                ),
                startRadius: 10,
                endRadius: 300
            )
            
            // Top aurora
            TopAurora()
                .offset(y: -20)
        }
        .ignoresSafeArea()
        .allowsHitTesting(false)

    }
}

#Preview {
    ContentView()
}
