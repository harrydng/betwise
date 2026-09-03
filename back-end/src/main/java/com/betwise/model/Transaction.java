package com.betwise.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {

    public enum TransactionType {
        BUY,
        SELL
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @Enumerated(EnumType.STRING)
    @Column(
        name = "transaction_type",
        nullable = false,
        length = 20
    )
    private TransactionType transactionType;

    @Column(
        nullable = false,
        precision = 19,
        scale = 6
    )
    private BigDecimal quantity;

    @Column(
        nullable = false,
        precision = 15,
        scale = 2
    )
    private BigDecimal price;

    @Column(
        name = "total_amount",
        nullable = false,
        precision = 15,
        scale = 2
    )
    private BigDecimal totalAmount;

    @Column(name = "executed_at", nullable = false)
    private LocalDateTime executedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Transaction() {
    }

    public Transaction(
        Portfolio portfolio,
        Asset asset,
        TransactionType transactionType,
        BigDecimal quantity,
        BigDecimal price
    ) {
        this.portfolio = portfolio;
        this.asset = asset;
        this.transactionType = transactionType;
        this.quantity = quantity;
        this.price = price;

        this.totalAmount = quantity.multiply(price);
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();

        if (executedAt == null) {
            executedAt = LocalDateTime.now();
        }
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

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public LocalDateTime getExecutedAt() {
        return executedAt;
    }
}