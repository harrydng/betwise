package com.betwise.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "watchlist_items",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"watchlist_id", "asset_id"})
    }
)
public class WatchlistItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "watchlist_id", nullable = false)
    private Watchlist watchlist;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @Column(name = "added_at", nullable = false)
    private LocalDateTime addedAt;

    public WatchlistItem() {
    }

    public WatchlistItem(Watchlist watchlist, Asset asset) {
        this.watchlist = watchlist;
        this.asset = asset;
    }

    @PrePersist
    protected void onCreate() {
        addedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Watchlist getWatchlist() {
        return watchlist;
    }

    public Asset getAsset() {
        return asset;
    }

    public LocalDateTime getAddedAt() {
        return addedAt;
    }

    public void setWatchlist(Watchlist watchlist) {
        this.watchlist = watchlist;
    }

    public void setAsset(Asset asset) {
        this.asset = asset;
    }
}