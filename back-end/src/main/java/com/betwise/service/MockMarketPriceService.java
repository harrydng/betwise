package com.betwise.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Service
public class MockMarketPriceService
        implements MarketPriceService {

    private static final Map<String, BigDecimal> PRICES =
            Map.of(
                    "AAPL", new BigDecimal("200.00"),
                    "NVDA", new BigDecimal("180.00"),
                    "MSFT", new BigDecimal("420.00"),
                    "VOO", new BigDecimal("550.00")
            );

    @Override
    public BigDecimal getCurrentPrice(String ticker) {

        return PRICES.getOrDefault(
                ticker.toUpperCase(),
                BigDecimal.ZERO
        );
    }
}