import SwiftUI

struct SearchView: View {
    @Bindable var model: MarketModel
    let portfolio: PortfolioModel

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 14) {
                if model.searchResults.isEmpty {
                    ContentUnavailableView.search(text: model.searchText)
                        .padding(.top, 60)
                } else {
                    SectionHeader(title: model.searchText.isEmpty ? "All assets" : "Results")

                    SearchResultList(rows: model.searchResults)
                }
            }
            .padding(.horizontal, 20)
            .padding(.bottom, 32)
        }
        .scrollIndicators(.hidden)
        .scrollDismissesKeyboard(.immediately)
        .screenBackground()
        .navigationTitle("Search")
        .searchable(text: $model.searchText, prompt: "Stocks, ETFs, bonds and REITs")
        .assetDestinations(market: model, portfolio: portfolio)
    }
}

private struct SearchResultList: View {
    let rows: [MarketRowItem]

    var body: some View {
        LazyVStack(spacing: 0) {
            ForEach(rows) { row in
                NavigationLink(value: row) {
                    AssetRow(
                        ticker: row.ticker,
                        name: row.name,
                        price: row.price,
                        changePercent: row.dayChangePercent,
                        sparkline: row.sparkline
                    )
                }
                .buttonStyle(.plain)
            }
        }
        .card(padding: 16)
    }
}

#Preview {
    NavigationStack {
        SearchView(model: MarketModel(usesSampleData: true), portfolio: PortfolioModel(usesSampleData: true))
    }
    .preferredColorScheme(.dark)
}
