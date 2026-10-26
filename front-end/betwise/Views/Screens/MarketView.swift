import SwiftUI

struct MarketView: View {
    @StateObject private var viewModel = MarketViewModel()

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 20) {
                VStack(alignment: .leading, spacing: 6) {
                    Text("Market")
                        .font(.system(size: 34, weight: .bold))
                        .foregroundStyle(AppColors.textPrimary)

                    Text("Your watchlist")
                        .font(.system(size: 16))
                        .foregroundStyle(AppColors.textSecondary)
                }

                HStack(spacing: 12) {
                    statCard(title: "S&P 500", value: "5,842.17", change: "+0.61%")
                    statCard(title: "NASDAQ", value: "18,573", change: "+0.94%")
                }

                ForEach(viewModel.stocks) { stock in
                    StockCard(stock: stock)
                }

                if let error = viewModel.errorMessage {
                    Text("Backend: \(error)")
                        .font(.caption)
                        .foregroundStyle(AppColors.textSecondary)
                }
            }
            .padding(.horizontal, 20)
            .padding(.top, 26)
            .padding(.bottom, 120)
        }
        .task {
            // This already calls GET /api/watchlists.
            // Until login is wired into the UI, an unauthorized message is expected.
            await viewModel.loadWatchlists()
        }
    }

    private func statCard(title: String, value: String, change: String) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(title)
                .font(.caption)
                .foregroundStyle(AppColors.textSecondary)

            Text(value)
                .font(.system(size: 18, weight: .bold))
                .foregroundStyle(AppColors.textPrimary)

            Text(change)
                .font(.caption.weight(.bold))
                .foregroundStyle(AppColors.positive)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(16)
        .background(AppColors.surface)
        .clipShape(RoundedRectangle(cornerRadius: 20, style: .continuous))
    }
}

#Preview {
    ContentView()
}
