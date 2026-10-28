package com.betwise.service;

import com.betwise.dto.PricePointResponse;
import com.betwise.model.PriceRange;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Where prices come from. Swap the implementation for a real market data
 * provider without touching the rest of the app.
 */
public interface MarketPriceService {

    BigDecimal getCurrentPrice(String ticker);

    /** Today's move in percent, where 2.5 means +2.5%. */
    BigDecimal getDayChangePercent(String ticker);

    /**
     * Prices at each of `range.timestamps(end)`. The last point is always
     * the current price.
     */
    List<PricePointResponse> getPriceHistory(
            String ticker,
            PriceRange range,
            LocalDateTime end);
}
