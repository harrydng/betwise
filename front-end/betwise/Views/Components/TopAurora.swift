import SwiftUI

struct TopAurora: View {
    var body: some View {
        ZStack {
            auroraBand(
                colors: [
                    Color(red: 0/255, green: 140/255, blue: 255/255).opacity(0.30),
                    Color(red: 0/255, green: 220/255, blue: 255/255).opacity(0.16),
                    Color.clear
                ],
                width: 320,
                height: 180,
                rotation: -18,
                x: -110,
                y: -20,
                blur: 45
            )

            auroraBand(
                colors: [
                    Color(red: 60/255, green: 120/255, blue: 255/255).opacity(0.22),
                    Color(red: 0/255, green: 190/255, blue: 255/255).opacity(0.12),
                    Color.clear
                ],
                width: 360,
                height: 200,
                rotation: 14,
                x: 70,
                y: -10,
                blur: 50
            )

            auroraBand(
                colors: [
                    Color(red: 0/255, green: 110/255, blue: 255/255).opacity(0.18),
                    Color(red: 120/255, green: 220/255, blue: 255/255).opacity(0.10),
                    Color.clear
                ],
                width: 280,
                height: 170,
                rotation: -30,
                x: 120,
                y: 25,
                blur: 42
            )
        }
        .frame(maxWidth: .infinity)
        .frame(height: 260)
        .mask(
            LinearGradient(
                colors: [
                    .white,
                    .white.opacity(0.95),
                    .white.opacity(0.65),
                    .clear
                ],
                startPoint: .top,
                endPoint: .bottom
            )
        )
        .blendMode(.screen)
        .allowsHitTesting(false)
    }

    private func auroraBand(
        colors: [Color],
        width: CGFloat,
        height: CGFloat,
        rotation: Double,
        x: CGFloat,
        y: CGFloat,
        blur: CGFloat
    ) -> some View {
        Ellipse()
            .fill(
                LinearGradient(
                    colors: colors,
                    startPoint: .leading,
                    endPoint: .trailing
                )
            )
            .frame(width: width, height: height)
            .rotationEffect(.degrees(rotation))
            .offset(x: x, y: y)
            .blur(radius: blur)
    }
}


#Preview {
    ContentView()
}
