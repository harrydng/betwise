package com.betwise.service;

import com.betwise.dto.PricePointResponse;
import com.betwise.model.PriceRange;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Fixed prices and generated, repeatable price paths for development.
 * Replace with a real market data provider for production.
 */
@Service
public class MockMarketPriceService
        implements MarketPriceService {

    private static final Map<String, BigDecimal> PRICES = Map.ofEntries(
            Map.entry("AAPL", new BigDecimal("200.00")),
            Map.entry("NVDA", new BigDecimal("180.00")),
            Map.entry("MSFT", new BigDecimal("420.00")),
            Map.entry("VOO", new BigDecimal("550.00")),
            Map.entry("TSLA", new BigDecimal("238.45")),
            Map.entry("AMZN", new BigDecimal("186.30")),
            Map.entry("GOOGL", new BigDecimal("171.12")),
            Map.entry("QQQ", new BigDecimal("482.60")),
            Map.entry("BND", new BigDecimal("72.84")),
            Map.entry("TLT", new BigDecimal("91.15")),
            Map.entry("O", new BigDecimal("57.21")),
            Map.entry("VNQ", new BigDecimal("89.40"))
    );

    private static final Map<String, Double> DAY_CHANGES = Map.ofEntries(
            Map.entry("AAPL", 1.24),
            Map.entry("NVDA", 3.42),
            Map.entry("MSFT", 0.58),
            Map.entry("VOO", 0.41),
            Map.entry("TSLA", -2.17),
            Map.entry("AMZN", 0.92),
            Map.entry("GOOGL", -0.36),
            Map.entry("QQQ", 0.77),
            Map.entry("BND", 0.08),
            Map.entry("TLT", -0.31),
            Map.entry("O", -0.44),
            Map.entry("VNQ", 0.21)
    );

    @Override
    public BigDecimal getCurrentPrice(String ticker) {

        return PRICES.getOrDefault(
                ticker.toUpperCase(),
                BigDecimal.ZERO
        );
    }

    @Override
    public BigDecimal getDayChangePercent(String ticker) {
        return BigDecimal.valueOf(dayChange(ticker.toUpperCase()))
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public List<PricePointResponse> getPriceHistory(
            String ticker,
            PriceRange range,
            LocalDateTime end) {

        String key = ticker.toUpperCase();
        List<LocalDateTime> timestamps = range.timestamps(end);
        double endValue = getCurrentPrice(key).doubleValue();
        double[] values = generatePath(key, range, endValue);

        List<PricePointResponse> points = new ArrayList<>(values.length);

        for (int i = 0; i < values.length; i++) {
            points.add(new PricePointResponse(
                    timestamps.get(i),
                    BigDecimal.valueOf(values[i]).setScale(2, RoundingMode.HALF_UP)));
        }

        return points;
    }

    // MARK: - Path generation
    //
    // A random walk with momentum around a straight line from the start
    // price to the current price. Seeded by ticker and range, so every
    // request returns the same chart.

    private double[] generatePath(String ticker, PriceRange range, double endValue) {

        Random random = new Random((ticker + "-" + range.getLabel()).hashCode());
        int count = range.getPointCount();
        double startValue = endValue / (1 + rangeChange(ticker, range) / 100);
        double volatility = volatility(range);

        double noise = 0;
        double velocity = 0;
        double[] values = new double[count];

        for (int i = 0; i < count; i++) {
            double progress = (double) i / (count - 1);

            velocity = velocity * 0.82 + (random.nextDouble() * 2 - 1) * volatility * 0.4;
            noise = noise * 0.95 + velocity;

            // Fade the wiggle out at both ends so the path starts and ends
            // exactly on the expected prices.
            double envelope = Math.sin(progress * Math.PI);
            double trend = startValue + (endValue - startValue) * progress;

            values[i] = trend * (1 + noise * envelope);
        }

        values[count - 1] = endValue;

        return values;
    }

    private double dayChange(String ticker) {
        Double known = DAY_CHANGES.get(ticker);

        if (known != null) {
            return known;
        }

        return new Random(ticker.hashCode()).nextDouble() * 5 - 2.5;
    }

    private double rangeChange(String ticker, PriceRange range) {
        double day = dayChange(ticker);

        if (range == PriceRange.DAY) {
            return day;
        }

        double scale = switch (range) {
            case WEEK -> 1.6;
            case MONTH -> 3;
            case THREE_MONTHS -> 5.5;
            case YEAR -> 12;
            case ALL -> 28;
            default -> 1;
        };

        Random random = new Random((ticker + "-change-" + range.getLabel()).hashCode());
        double change = day * scale * 0.6 + (random.nextDouble() * 2.6 - 1) * scale;

        return Math.max(change, -60);
    }

    private double volatility(PriceRange range) {
        return switch (range) {
            case DAY -> 0.0035;
            case WEEK -> 0.005;
            case MONTH -> 0.007;
            case THREE_MONTHS -> 0.01;
            case YEAR -> 0.014;
            case ALL -> 0.02;
        };
    }
}
