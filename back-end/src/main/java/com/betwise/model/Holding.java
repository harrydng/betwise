package com.betwise.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "holdings",
    uniqueConstraints = {
        @UniqueConstraint(
            columnNames = {"portfolio_id", "asset_id"}
        )
    }
)
public class Holding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @Column(
        nullable = false,
        precision = 19,
        scale = 6
    )
    private BigDecimal quantity;

    @Column(
        name = "average_cost",
        nullable = false,
        precision = 15,
        scale = 2
    )
    private BigDecimal averageCost;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Holding() {
    }

    public Holding(
        Portfolio portfolio,
        Asset asset,
        BigDecimal quantity,
        BigDecimal averageCost
    ) {
        this.portfolio = portfolio;
        this.asset = asset;
        this.quantity = quantity;
        this.averageCost = averageCost;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Portfolio getPortfolio() {
        return portfolio;
    }

    public Asset getAsset() {
        return asset;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public BigDecimal getAverageCost() {
        return averageCost;
    }

    public void setPortfolio(Portfolio portfolio) {
        this.portfolio = portfolio;
    }

    public void setAsset(Asset asset) {
        this.asset = asset;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public void setAverageCost(BigDecimal averageCost) {
        this.averageCost = averageCost;
    }
}