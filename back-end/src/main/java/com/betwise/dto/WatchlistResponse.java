package com.betwise.dto;

import com.betwise.model.Watchlist;
import com.betwise.model.WatchlistItem;

import java.time.LocalDateTime;
import java.util.List;

public class WatchlistResponse {

    private Long id;
    private String name;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<WatchlistItemResponse> items;

    public WatchlistResponse(
            Watchlist watchlist,
            List<WatchlistItem> items
    ) {
        this.id = watchlist.getId();
        this.name = watchlist.getName();
        this.createdAt = watchlist.getCreatedAt();
        this.updatedAt = watchlist.getUpdatedAt();

        this.items = items.stream()
                .map(WatchlistItemResponse::new)
                .toList();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public List<WatchlistItemResponse> getItems() {
        return items;
    }
}