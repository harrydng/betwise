import SwiftUI

struct PortfolioView: View {
    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 22) {
                Text("Portfolio")
                    .font(.system(size: 34, weight: .bold))
                    .foregroundStyle(AppColors.textPrimary)

                VStack(alignment: .leading, spacing: 8) {
                    Text("Total value")
                        .foregroundStyle(AppColors.textSecondary)

                    Text("$12,840.62")
                        .font(.system(size: 38, weight: .bold))
                        .foregroundStyle(AppColors.textPrimary)

                    Text("+$1,284.03  •  +11.11%")
                        .font(.system(size: 15, weight: .semibold))
                        .foregroundStyle(AppColors.positive)
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(22)
                .background(AppColors.surface)
                .clipShape(RoundedRectangle(cornerRadius: 26, style: .continuous))

                Text("Positions")
                    .font(.title3.bold())
                    .foregroundStyle(AppColors.textPrimary)

                portfolioRow(symbol: "AAPL", name: "Apple", value: "$5,000", change: "+150.00%")
                portfolioRow(symbol: "NVDA", name: "NVIDIA", value: "$4,800", change: "+50.00%")
                portfolioRow(symbol: "TSLA", name: "Tesla", value: "$3,040", change: "-8.46%")
            }
            .padding(.horizontal, 20)
            .padding(.top, 26)
            .padding(.bottom, 120)
        }
    }

    private func portfolioRow(symbol: String, name: String, value: String, change: String) -> some View {
        HStack {
            VStack(alignment: .leading, spacing: 4) {
                Text(symbol)
                    .font(.headline)
                    .foregroundStyle(AppColors.textPrimary)
                Text(name)
                    .font(.caption)
                    .foregroundStyle(AppColors.textSecondary)
            }

            Spacer()

            VStack(alignment: .trailing, spacing: 4) {
                Text(value)
                    .font(.headline)
                    .foregroundStyle(AppColors.textPrimary)
                Text(change)
                    .font(.caption.bold())
                    .foregroundStyle(change.hasPrefix("-") ? AppColors.negative : AppColors.positive)
            }
        }
        .padding(17)
        .background(AppColors.surface)
        .clipShape(RoundedRectangle(cornerRadius: 20, style: .continuous))
    }
}

#Preview {
    ContentView()
}
