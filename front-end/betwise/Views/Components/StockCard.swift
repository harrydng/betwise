import SwiftUI
import Charts

struct StockCard: View {
    let stock: StockPosition

    var body: some View {
        VStack(alignment: .leading, spacing: 14) {
            HStack(alignment: .top) {
                VStack(alignment: .leading, spacing: 3) {
                    Text(stock.symbol)
                        .font(.system(size: 20, weight: .bold))
                        .foregroundStyle(AppColors.textPrimary)

                    Text(stock.companyName)
                        .font(.system(size: 13))
                        .foregroundStyle(AppColors.textSecondary)
                }

                Spacer()

                Text(String(format: "%+.2f%%", stock.percentageChange))
                    .font(.system(size: 14, weight: .bold))
                    .foregroundStyle(stock.isPositive ? AppColors.positive : AppColors.negative)
            }

            Chart {
                ForEach(Array(stock.priceHistory.enumerated()), id: \.offset) { index, value in
                    LineMark(
                        x: .value("Index", index),
                        y: .value("Price", value)
                    )
                    .interpolationMethod(.catmullRom)
                    .foregroundStyle(stock.isPositive ? AppColors.positive : AppColors.negative)
                }
            }
            .chartXAxis(.hidden)
            .chartYAxis(.hidden)
            .frame(height: 74)

            HStack {
                VStack(alignment: .leading, spacing: 3) {
                    Text("Bought")
                        .font(.caption)
                        .foregroundStyle(AppColors.textSecondary)

                    Text(stock.purchasePrice, format: .currency(code: "USD"))
                        .font(.system(size: 16, weight: .semibold))
                        .foregroundStyle(AppColors.textPrimary)
                }

                Spacer()

                VStack(alignment: .trailing, spacing: 3) {
                    Text("Current")
                        .font(.caption)
                        .foregroundStyle(AppColors.textSecondary)

                    Text(stock.currentPrice, format: .currency(code: "USD"))
                        .font(.system(size: 16, weight: .semibold))
                        .foregroundStyle(AppColors.textPrimary)
                }
            }
        }
        .padding(18)
        .background(AppColors.surface)
        .clipShape(RoundedRectangle(cornerRadius: 24, style: .continuous))
    }
}

#Preview {
    ContentView()
}
