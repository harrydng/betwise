package com.betwise.dto;

import com.betwise.model.Portfolio;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class PortfolioResponse {

    private Long id;
    private String name;

    private BigDecimal startingBalance;

    private BigDecimal cashAvailable;

    private BigDecimal holdingsValue;

    private BigDecimal currentPortfolioValue;

    private BigDecimal investedCostBasis;

    private BigDecimal unrealizedGain;

    private BigDecimal unrealizedGainPercent;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<HoldingResponse> holdings;

    public PortfolioResponse(
            Portfolio portfolio,
            List<HoldingResponse> holdings) {

        this.id = portfolio.getId();
        this.name = portfolio.getName();

        this.startingBalance = portfolio.getStartingBalance();

        this.cashAvailable = portfolio.getCashBalance();

        this.holdings = holdings;

        this.holdingsValue = holdings.stream()
                .map(holding -> holding.getMarketValue())
                .reduce(
                        BigDecimal.ZERO,
                        (a, b) -> a.add(b))
                .setScale(
                        2,
                        java.math.RoundingMode.HALF_UP);

        this.investedCostBasis = holdings.stream()
                .map(holding -> holding.getCostBasis())
                .reduce(
                        BigDecimal.ZERO,
                        (a, b) -> a.add(b))
                .setScale(
                        2,
                        java.math.RoundingMode.HALF_UP);

        this.currentPortfolioValue = cashAvailable
                .add(holdingsValue);

        this.unrealizedGain = holdingsValue
                .subtract(investedCostBasis);

        if (investedCostBasis.compareTo(
                BigDecimal.ZERO) > 0) {

            this.unrealizedGainPercent = unrealizedGain
                    .divide(
                            investedCostBasis,
                            6,
                            java.math.RoundingMode.HALF_UP)
                    .multiply(
                            BigDecimal.valueOf(100))
                    .setScale(
                            2,
                            java.math.RoundingMode.HALF_UP);

        } else {

            this.unrealizedGainPercent = BigDecimal.ZERO;
        }

        this.createdAt = portfolio.getCreatedAt();

        this.updatedAt = portfolio.getUpdatedAt();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getStartingBalance() {
        return startingBalance;
    }

    public BigDecimal getCashAvailable() {
        return cashAvailable;
    }

    public BigDecimal getHoldingsValue() {
        return holdingsValue;
    }

    public BigDecimal getCurrentPortfolioValue() {
        return currentPortfolioValue;
    }

    public BigDecimal getInvestedCostBasis() {
        return investedCostBasis;
    }

    public BigDecimal getUnrealizedGain() {
        return unrealizedGain;
    }

    public BigDecimal getUnrealizedGainPercent() {
        return unrealizedGainPercent;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public List<HoldingResponse> getHoldings() {
        return holdings;
    }
}