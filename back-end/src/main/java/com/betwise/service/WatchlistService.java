package com.betwise.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.betwise.dto.AddWatchlistItemRequest;
import com.betwise.dto.CreateWatchlistRequest;
import com.betwise.dto.UpdateWatchlistRequest;
import com.betwise.dto.WatchlistResponse;
import com.betwise.model.Asset;
import com.betwise.model.User;
import com.betwise.model.Watchlist;
import com.betwise.model.WatchlistItem;
import com.betwise.repository.AssetRepository;
import com.betwise.repository.WatchlistItemRepository;
import com.betwise.repository.WatchlistRepository;

import jakarta.transaction.Transactional;

@Service
public class WatchlistService {

    private final WatchlistRepository watchlistRepository;
    private final WatchlistItemRepository watchlistItemRepository;
    private final AssetRepository assetRepository;

    public WatchlistService(
            WatchlistRepository watchlistRepository,
            WatchlistItemRepository watchlistItemRepository,
            AssetRepository assetRepository) {
        this.watchlistRepository = watchlistRepository;
        this.watchlistItemRepository = watchlistItemRepository;
        this.assetRepository = assetRepository;
    }

    public WatchlistResponse createWatchlist(
            User user,
            CreateWatchlistRequest request) {

        String name = request.getName().trim();

        if (name.isEmpty()) {
            throw new IllegalArgumentException(
                    "Watchlist name cannot be empty");
        }

        if (watchlistRepository.existsByNameAndUserId(
                name,
                user.getId())) {
            throw new IllegalArgumentException(
                    "Watchlist name already exists");
        }

        Watchlist watchlist = new Watchlist(user, name);

        Watchlist saved = watchlistRepository.save(watchlist);

        return new WatchlistResponse(
                saved,
                List.of());
    }

    public List<WatchlistResponse> getWatchlists(
            User user) {

        return watchlistRepository.findByUserId(user.getId())
                .stream()
                .map(watchlist -> new WatchlistResponse(
                        watchlist,
                        watchlistItemRepository
                                .findByWatchlistId(
                                        watchlist.getId())))
                .toList();
    }

    public WatchlistResponse getWatchlist(
            User user,
            Long watchlistId) {

        Watchlist watchlist = getOwnedWatchlist(
                watchlistId,
                user.getId());

        List<WatchlistItem> items = watchlistItemRepository
                .findByWatchlistId(watchlistId);

        return new WatchlistResponse(
                watchlist,
                items);
    }

    public WatchlistResponse updateWatchlist(
            User user,
            Long watchlistId,
            UpdateWatchlistRequest request) {

        Watchlist watchlist = getOwnedWatchlist(
                watchlistId,
                user.getId());

        if (request.getName() != null) {

            String name = request.getName().trim();

            if (name.isEmpty()) {
                throw new IllegalArgumentException(
                        "Watchlist name cannot be empty");
            }

            if (!name.equals(watchlist.getName())
                    && watchlistRepository
                            .existsByNameAndUserId(
                                    name,
                                    user.getId())) {

                throw new IllegalArgumentException(
                        "Watchlist name already exists");
            }

            watchlist.setName(name);
        }

        Watchlist updated = watchlistRepository.save(watchlist);

        List<WatchlistItem> items = watchlistItemRepository
                .findByWatchlistId(watchlistId);

        return new WatchlistResponse(
                updated,
                items);
    }

    @Transactional
    public void deleteWatchlist(
            User user,
            Long watchlistId) {

        Watchlist watchlist = getOwnedWatchlist(
                watchlistId,
                user.getId());

        watchlistItemRepository
                .deleteByWatchlistId(watchlistId);

        watchlistRepository.delete(watchlist);
    }

    public WatchlistResponse addItem(
            User user,
            Long watchlistId,
            AddWatchlistItemRequest request) {

        Watchlist watchlist = getOwnedWatchlist(
                watchlistId,
                user.getId());

        Asset asset = assetRepository.findById(
                request.getAssetId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Asset not found"));

        if (watchlistItemRepository
                .existsByWatchlistIdAndAssetId(
                        watchlistId,
                        asset.getId())) {

            throw new IllegalArgumentException(
                    "Asset already exists in watchlist");
        }

        WatchlistItem item = new WatchlistItem(
                watchlist,
                asset);

        watchlistItemRepository.save(item);

        List<WatchlistItem> items = watchlistItemRepository
                .findByWatchlistId(watchlistId);

        return new WatchlistResponse(
                watchlist,
                items);
    }

    public void removeItem(
            User user,
            Long watchlistId,
            Long itemId) {

        getOwnedWatchlist(
                watchlistId,
                user.getId());

        WatchlistItem item = watchlistItemRepository
                .findByIdAndWatchlistId(
                        itemId,
                        watchlistId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Watchlist item not found"));

        watchlistItemRepository.delete(item);
    }

    private Watchlist getOwnedWatchlist(
            Long watchlistId,
            Long userId) {

        return watchlistRepository
                .findByIdAndUserId(
                        watchlistId,
                        userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Watchlist not found"));
    }
}