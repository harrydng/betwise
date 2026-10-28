package com.betwise.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * A chart series for an asset or a portfolio over one range.
 */
public class PriceHistoryResponse {

    private String range;
    private BigDecimal changePercent;
    private List<PricePointResponse> points;

    public PriceHistoryResponse(
            String range,
            List<PricePointResponse> points) {
        this.range = range;
        this.points = points;
        this.changePercent = computeChangePercent(points);
    }

    public String getRange() {
        return range;
    }

    /** The move from the first point to the last, in percent. */
    public BigDecimal getChangePercent() {
        return changePercent;
    }

    public List<PricePointResponse> getPoints() {
        return points;
    }

    private static BigDecimal computeChangePercent(
            List<PricePointResponse> points) {

        if (points.size() < 2) {
            return BigDecimal.ZERO;
        }

        BigDecimal first = points.get(0).getValue();
        BigDecimal last = points.get(points.size() - 1).getValue();

        if (first.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return last.subtract(first)
                .divide(first, 6, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, java.math.RoundingMode.HALF_UP);
    }
}
