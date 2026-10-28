// ViewModels/MarketModel.swift

import Foundation
import Observation

enum AssetFilter: Hashable {
    case all
    case type(Asset.AssetType)
}

@Observable
final class MarketModel {
    private(set) var indices: [MarketIndex] = SampleData.indices

    private(set) var rows: [MarketRowItem] = [] {
        didSet { rebuildDerivedRows() }
    }

    private(set) var watchlistAssetIDs: Set<Int> = [] {
        didSet { rebuildWatchlist() }
    }

    var filter: AssetFilter = .all {
        didSet { rebuildVisibleRows() }
    }

    var searchText = "" {
        didSet { rebuildSearchResults() }
    }

    // Derived lists, cached so views only iterate prepared arrays.
    private(set) var visibleRows: [MarketRowItem] = []
    private(set) var watchlistRows: [MarketRowItem] = []
    private(set) var searchResults: [MarketRowItem] = []
    private(set) var rowsByAssetID: [Int: MarketRowItem] = [:]

    private(set) var errorMessage: String?

    /// The user's server-side watchlist, once signed in.
    private var watchlist: Watchlist?

    static let filterOptions: [FilterOption<AssetFilter>] =
        [FilterOption(value: .all, title: String(localized: "All"))]
        + Asset.AssetType.allCases.map { FilterOption(value: .type($0), title: $0.pluralName) }

    /// Guests browse sample assets; signed-in users only see the backend's.
    let usesSampleData: Bool

    private(set) var hasLoaded: Bool

    init(usesSampleData: Bool) {
        self.usesSampleData = usesSampleData
        hasLoaded = usesSampleData

        if usesSampleData {
            rows = SampleData.assets.map { Self.makeRow($0) }
            watchlistAssetIDs = SampleData.watchlistAssetIDs
        }

        // Property observers don't run during init.
        rebuildDerivedRows()
    }

    func isWatching(_ assetID: Int) -> Bool {
        watchlistAssetIDs.contains(assetID)
    }

    func refresh() async {
        guard !usesSampleData else { return }

        defer { hasLoaded = true }

        do {
            rows = try await AssetService.shared.getAssets().map { Self.makeRow($0) }

            watchlist = try await WatchlistService.shared.getWatchlists().first
            watchlistAssetIDs = Set(watchlist?.items.map(\.assetId) ?? [])
            errorMessage = nil
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    /// Adds or removes an asset. The star updates immediately and rolls
    /// back if the server rejects the change.
    func toggleWatchlist(_ assetID: Int) async {
        let wasWatching = watchlistAssetIDs.contains(assetID)

        if wasWatching {
            watchlistAssetIDs.remove(assetID)
        } else {
            watchlistAssetIDs.insert(assetID)
        }

        guard !usesSampleData else { return }

        do {
            if wasWatching {
                if let watchlist,
                   let item = watchlist.items.first(where: { $0.assetId == assetID }) {
                    try await WatchlistService.shared.removeItem(watchlistId: watchlist.id, itemId: item.id)
                    self.watchlist = try await WatchlistService.shared.getWatchlist(id: watchlist.id)
                }
            } else {
                let target = try await existingOrNewWatchlist()
                watchlist = try await WatchlistService.shared.addItem(watchlistId: target.id, assetId: assetID)
            }
        } catch {
            if wasWatching {
                watchlistAssetIDs.insert(assetID)
            } else {
                watchlistAssetIDs.remove(assetID)
            }

            errorMessage = error.localizedDescription
        }
    }

    private func existingOrNewWatchlist() async throws -> Watchlist {
        if let watchlist {
            return watchlist
        }

        let created = try await WatchlistService.shared.createWatchlist(name: "My Watchlist")
        watchlist = created
        return created
    }

    // MARK: - Derived data

    private func rebuildDerivedRows() {
        rowsByAssetID = Dictionary(uniqueKeysWithValues: rows.map { ($0.id, $0) })
        rebuildVisibleRows()
        rebuildWatchlist()
        rebuildSearchResults()
    }

    private func rebuildVisibleRows() {
        switch filter {
        case .all:
            visibleRows = rows
        case .type(let type):
            visibleRows = rows.filter { $0.assetType == type }
        }
    }

    private func rebuildWatchlist() {
        watchlistRows = rows.filter { watchlistAssetIDs.contains($0.id) }
    }

    private func rebuildSearchResults() {
        let query = searchText.trimmingCharacters(in: .whitespaces)

        guard !query.isEmpty else {
            searchResults = rows
            return
        }

        searchResults = rows.filter {
            $0.ticker.localizedCaseInsensitiveContains(query)
                || $0.name.localizedCaseInsensitiveContains(query)
        }
    }

    private static func makeRow(_ asset: Asset) -> MarketRowItem {
        // Backend assets carry today's move and sparkline; sample assets
        // fall back to generated ones.
        let change = asset.dayChangePercent?.doubleValue
            ?? SampleData.dayChange(for: asset.ticker)

        let sparkline: [PricePoint]

        if let serverSparkline = asset.sparkline, serverSparkline.count > 1 {
            sparkline = serverSparkline.enumerated().map { index, value in
                PricePoint(id: index, value: value.doubleValue)
            }
        } else {
            sparkline = PriceHistory.series(
                seed: asset.ticker,
                range: .day,
                endValue: asset.currentPrice.doubleValue,
                changePercent: change
            )
        }

        let values = sparkline.map(\.value)

        return MarketRowItem(
            id: asset.id,
            ticker: asset.ticker,
            name: asset.name,
            assetType: asset.assetType,
            price: asset.currentPrice,
            dayChangePercent: change,
            sparkline: sparkline,
            dayLow: values.min() ?? 0,
            dayHigh: values.max() ?? 0
        )
    }
}
