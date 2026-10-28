import SwiftUI

/// The raised, hairline-bordered surface every card in the app sits on.
struct CardBackground: ViewModifier {
    var padding: CGFloat
    var cornerRadius: CGFloat

    func body(content: Content) -> some View {
        content
            .padding(padding)
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(
                AppColors.surface,
                in: .rect(cornerRadius: cornerRadius, style: .continuous)
            )
            .overlay {
                RoundedRectangle(cornerRadius: cornerRadius, style: .continuous)
                    .strokeBorder(AppColors.stroke, lineWidth: 1)
            }
    }
}

extension View {
    func card(padding: CGFloat = 18, cornerRadius: CGFloat = 24) -> some View {
        modifier(CardBackground(padding: padding, cornerRadius: cornerRadius))
    }

    /// The black backdrop shared by every screen.
    func screenBackground() -> some View {
        background(AppColors.background.ignoresSafeArea())
    }
}
