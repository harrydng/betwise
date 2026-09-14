package com.betwise.dto;

import java.math.BigDecimal;

public class TransferCashRequest {

    private Long destinationPortfolioId;
    private BigDecimal amount;

    public Long getDestinationPortfolioId() {
        return destinationPortfolioId;
    }

    public void setDestinationPortfolioId(
            Long destinationPortfolioId) {
        this.destinationPortfolioId =
                destinationPortfolioId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}