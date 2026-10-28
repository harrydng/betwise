package com.betwise.service;

import com.betwise.dto.PricePointResponse;
import com.betwise.model.PriceRange;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MockMarketPriceServiceTest {

    private final MockMarketPriceService service = new MockMarketPriceService();

    private final LocalDateTime end = LocalDateTime.of(2026, 10, 28, 16, 0);

    @Test
    void historyHasOnePointPerTimestampAndEndsNow() {

        for (PriceRange range : PriceRange.values()) {
            List<PricePointResponse> points = service.getPriceHistory("AAPL", range, end);

            assertEquals(range.getPointCount(), points.size(), range.getLabel());
            assertEquals(end, points.get(points.size() - 1).getTimestamp(), range.getLabel());
        }
    }

    @Test
    void historyEndsAtTheCurrentPrice() {

        List<PricePointResponse> points = service.getPriceHistory("NVDA", PriceRange.MONTH, end);

        assertEquals(
                0,
                new BigDecimal("180.00").compareTo(points.get(points.size() - 1).getValue()));
    }

    @Test
    void dayHistoryStartsAtThePriceImpliedByTheDayChange() {

        List<PricePointResponse> points = service.getPriceHistory("AAPL", PriceRange.DAY, end);

        // AAPL is $200.00, up 1.24% today, so it opened near 200 / 1.0124.
        double open = points.get(0).getValue().doubleValue();

        assertEquals(200 / 1.0124, open, 0.01);
    }

    @Test
    void historyIsRepeatable() {

        List<PricePointResponse> first = service.getPriceHistory("TSLA", PriceRange.YEAR, end);
        List<PricePointResponse> second = service.getPriceHistory("TSLA", PriceRange.YEAR, end);

        for (int i = 0; i < first.size(); i++) {
            assertEquals(first.get(i).getValue(), second.get(i).getValue());
        }
    }

    @Test
    void unknownRangeLabelIsRejected() {

        assertThrows(IllegalArgumentException.class, () -> PriceRange.fromLabel("2D"));
        assertEquals(PriceRange.THREE_MONTHS, PriceRange.fromLabel("3m"));
    }
}
