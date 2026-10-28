package com.betwise.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PricePointResponse {

    private LocalDateTime timestamp;
    private BigDecimal value;

    public PricePointResponse(
            LocalDateTime timestamp,
            BigDecimal value) {
        this.timestamp = timestamp;
        this.value = value;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public BigDecimal getValue() {
        return value;
    }
}
