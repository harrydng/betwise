package com.betwise.dto;

import com.betwise.model.Asset;
import com.betwise.model.WatchlistItem;

import java.time.LocalDateTime;

public class WatchlistItemResponse {

    private Long id;
    private Long assetId;
    private String ticker;
    private String name;
    private Asset.AssetType assetType;
    private LocalDateTime addedAt;

    public WatchlistItemResponse(WatchlistItem item) {

        Asset asset = item.getAsset();

        this.id = item.getId();
        this.assetId = asset.getId();
        this.ticker = asset.getTicker();
        this.name = asset.getName();
        this.assetType = asset.getAssetType();
        this.addedAt = item.getAddedAt();
    }

    public Long getId() {
        return id;
    }

    public Long getAssetId() {
        return assetId;
    }

    public String getTicker() {
        return ticker;
    }

    public String getName() {
        return name;
    }

    public Asset.AssetType getAssetType() {
        return assetType;
    }

    public LocalDateTime getAddedAt() {
        return addedAt;
    }
}