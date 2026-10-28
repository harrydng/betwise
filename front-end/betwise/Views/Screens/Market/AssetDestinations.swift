import SwiftUI

extension View {
    /// Lets any screen push an asset's detail page with
    /// `NavigationLink(value: MarketRowItem)`.
    func assetDestinations(market: MarketModel, portfolio: PortfolioModel) -> some View {
        navigationDestination(for: MarketRowItem.self) { item in
            AssetDetailView(item: item, market: market, portfolio: portfolio)
        }
    }
}
