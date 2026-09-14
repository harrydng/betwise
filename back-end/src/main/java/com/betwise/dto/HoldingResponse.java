package com.betwise.dto;

import com.betwise.model.Asset;
import com.betwise.model.Holding;

import java.math.BigDecimal;

public class HoldingResponse {

    private Long id;
    private Long assetId;

    private String ticker;
    private String name;

    private Asset.AssetType assetType;

    private BigDecimal quantity;
    private BigDecimal averageCost;

    private BigDecimal currentPrice;

    private BigDecimal costBasis;
    private BigDecimal marketValue;

    private BigDecimal unrealizedGain;
    private BigDecimal unrealizedGainPercent;

    public HoldingResponse(
            Holding holding,
            BigDecimal currentPrice) {

        Asset asset = holding.getAsset();

        this.id = holding.getId();
        this.assetId = asset.getId();

        this.ticker = asset.getTicker();
        this.name = asset.getName();
        this.assetType = asset.getAssetType();

        this.quantity = holding.getQuantity();
        this.averageCost = holding.getAverageCost();

        this.currentPrice = currentPrice;

        this.costBasis =
                quantity
                        .multiply(averageCost)
                        .setScale(
                                2,
                                java.math.RoundingMode.HALF_UP);

        this.marketValue =
                quantity
                        .multiply(currentPrice)
                        .setScale(
                                2,
                                java.math.RoundingMode.HALF_UP);

        this.unrealizedGain =
                marketValue
                        .subtract(costBasis)
                        .setScale(
                                2,
                                java.math.RoundingMode.HALF_UP);

        if (costBasis.compareTo(BigDecimal.ZERO) > 0) {

            this.unrealizedGainPercent =
                    unrealizedGain
                            .divide(
                                    costBasis,
                                    6,
                                    java.math.RoundingMode.HALF_UP)
                            .multiply(
                                    BigDecimal.valueOf(100))
                            .setScale(
                                    2,
                                    java.math.RoundingMode.HALF_UP);

        } else {

            this.unrealizedGainPercent =
                    BigDecimal.ZERO;
        }
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

    public BigDecimal getQuantity() {
        return quantity;
    }

    public BigDecimal getAverageCost() {
        return averageCost;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public BigDecimal getCostBasis() {
        return costBasis;
    }

    public BigDecimal getMarketValue() {
        return marketValue;
    }

    public BigDecimal getUnrealizedGain() {
        return unrealizedGain;
    }

    public BigDecimal getUnrealizedGainPercent() {
        return unrealizedGainPercent;
    }
}