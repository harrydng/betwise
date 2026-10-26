import SwiftUI

struct TradeView: View {
    @State private var symbol = ""
    @State private var amount = ""

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 22) {
                Text("Trade")
                    .font(.system(size: 34, weight: .bold))
                    .foregroundStyle(AppColors.textPrimary)

                Text("Practice investing with your Betwise balance.")
                    .foregroundStyle(AppColors.textSecondary)

                VStack(spacing: 14) {
                    textField(title: "Stock symbol", text: $symbol)
                    textField(title: "Amount", text: $amount)

                    Button {
                        print("Trade \(symbol) \(amount)")
                    } label: {
                        Text("Preview trade")
                            .font(.headline)
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 16)
                            .foregroundStyle(.white)
                            .background(AppColors.primaryBlue)
                            .clipShape(RoundedRectangle(cornerRadius: 18, style: .continuous))
                    }
                }
                .padding(18)
                .background(AppColors.surface)
                .clipShape(RoundedRectangle(cornerRadius: 24, style: .continuous))
            }
            .padding(.horizontal, 20)
            .padding(.top, 26)
            .padding(.bottom, 120)
        }
    }

    private func textField(title: String, text: Binding<String>) -> some View {
        TextField(title, text: text)
            .padding(15)
            .foregroundStyle(AppColors.textPrimary)
            .background(AppColors.surfaceLight)
            .clipShape(
                RoundedRectangle(
                    cornerRadius: 15,
                    style: .continuous
                )
            )
    }
}

#Preview {
    ContentView()
}
