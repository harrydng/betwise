package com.betwise.dto;

import com.betwise.model.Asset;

import java.math.BigDecimal;

public class AssetResponse {

    private Long id;
    private String ticker;
    private String name;
    private Asset.AssetType assetType;
    private BigDecimal currentPrice;

    public AssetResponse(
            Asset asset,
            BigDecimal currentPrice) {

        this.id = asset.getId();
        this.ticker = asset.getTicker();
        this.name = asset.getName();
        this.assetType = asset.getAssetType();
        this.currentPrice = currentPrice;
    }

    public Long getId() {
        return id;
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

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }
}