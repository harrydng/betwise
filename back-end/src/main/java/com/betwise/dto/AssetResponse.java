package com.betwise.dto;

import com.betwise.model.Asset;

import java.math.BigDecimal;
import java.util.List;

public class AssetResponse {

    private Long id;
    private String ticker;
    private String name;
    private Asset.AssetType assetType;
    private BigDecimal currentPrice;
    private BigDecimal dayChangePercent;

    // Today's prices, thinned out for small list-row charts. Use
    // GET /api/assets/{id}/history for a full chart.
    private List<BigDecimal> sparkline;

    public AssetResponse(
            Asset asset,
            BigDecimal currentPrice,
            BigDecimal dayChangePercent,
            List<BigDecimal> sparkline) {

        this.id = asset.getId();
        this.ticker = asset.getTicker();
        this.name = asset.getName();
        this.assetType = asset.getAssetType();
        this.currentPrice = currentPrice;
        this.dayChangePercent = dayChangePercent;
        this.sparkline = sparkline;
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

    public BigDecimal getDayChangePercent() {
        return dayChangePercent;
    }

    public List<BigDecimal> getSparkline() {
        return sparkline;
    }
}
