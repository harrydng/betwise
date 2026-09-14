package com.betwise.dto;

import java.math.BigDecimal;

public class CreatePortfolioRequest {

    private String name;
    private BigDecimal startingBalance;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getStartingBalance() {
        return startingBalance;
    }

    public void setStartingBalance(BigDecimal startingBalance) {
        this.startingBalance = startingBalance;
    }
}