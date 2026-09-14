package com.betwise.dto;

import com.betwise.model.Transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionResponse {

    private Long id;
    private Long assetId;
    private String ticker;

    private Transaction.TransactionType transactionType;

    private BigDecimal quantity;
    private BigDecimal price;
    private BigDecimal totalAmount;

    private LocalDateTime executedAt;

    public TransactionResponse(Transaction transaction) {

        this.id = transaction.getId();
        this.assetId = transaction.getAsset().getId();
        this.ticker = transaction.getAsset().getTicker();
        this.transactionType =
                transaction.getTransactionType();

        this.quantity = transaction.getQuantity();
        this.price = transaction.getPrice();
        this.totalAmount = transaction.getTotalAmount();
        this.executedAt = transaction.getExecutedAt();
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

    public Transaction.TransactionType getTransactionType() {
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