package com.betwise.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "assets",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = "ticker")
    }
)
public class Asset {

    public enum AssetType {
        STOCK,
        ETF,
        BOND,
        REIT
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String ticker;

    @Column(nullable = false, length = 255)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "asset_type", nullable = false, length = 30)
    private AssetType assetType;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Asset() {
    }

    public Asset(
        String ticker,
        String name,
        AssetType assetType
    ) {
        this.ticker = ticker;
        this.name = name;
        this.assetType = assetType;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
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

    public AssetType getAssetType() {
        return assetType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAssetType(AssetType assetType) {
        this.assetType = assetType;
    }
}