package com.betwise.repository;

import com.betwise.model.WatchlistItem;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WatchlistItemRepository
        extends JpaRepository<WatchlistItem, Long> {

    void deleteByWatchlistId(Long watchlistId);

    List<WatchlistItem> findByWatchlistId(Long watchlistId);

    Optional<WatchlistItem> findByIdAndWatchlistId(
            Long id,
            Long watchlistId);

    boolean existsByWatchlistIdAndAssetId(
            Long watchlistId,
            Long assetId);
}