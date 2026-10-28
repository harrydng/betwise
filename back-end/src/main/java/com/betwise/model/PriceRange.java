package com.betwise.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * The chart ranges the app offers. Each one covers a span of time with a
 * fixed number of evenly spaced points.
 */
public enum PriceRange {

    DAY("1D", Duration.ofDays(1), 78),
    WEEK("1W", Duration.ofDays(7), 84),
    MONTH("1M", Duration.ofDays(30), 90),
    THREE_MONTHS("3M", Duration.ofDays(91), 92),
    YEAR("1Y", Duration.ofDays(365), 104),
    ALL("ALL", Duration.ofDays(365 * 5), 120);

    private final String label;
    private final Duration span;
    private final int pointCount;

    PriceRange(String label, Duration span, int pointCount) {
        this.label = label;
        this.span = span;
        this.pointCount = pointCount;
    }

    public String getLabel() {
        return label;
    }

    public int getPointCount() {
        return pointCount;
    }

    /** Parses "1D", "1W", … as sent by the app. */
    public static PriceRange fromLabel(String label) {
        for (PriceRange range : values()) {
            if (range.label.equalsIgnoreCase(label)) {
                return range;
            }
        }

        throw new IllegalArgumentException(
                "Unknown range: " + label + ". Use 1D, 1W, 1M, 3M, 1Y or ALL");
    }

    /** The point timestamps for this range, ending at `end`. */
    public List<LocalDateTime> timestamps(LocalDateTime end) {
        LocalDateTime start = end.minus(span);
        long stepSeconds = span.getSeconds() / (pointCount - 1);

        List<LocalDateTime> timestamps = new ArrayList<>(pointCount);

        for (int i = 0; i < pointCount - 1; i++) {
            timestamps.add(start.plusSeconds(stepSeconds * i));
        }

        timestamps.add(end);

        return timestamps;
    }
}
