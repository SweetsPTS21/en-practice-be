package com.swpts.enpracticebe.util;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

public class DateUtil {
    public static Instant getStartOfPeriod(String period) {
        return switch (period) {
            case "day" -> getStartOfToday();
            case "week" -> getStartOfWeek();
            case "month" -> getStartOfMonth();
            default -> throw new IllegalArgumentException("Invalid period: " + period);
        };
    }

    public static Instant getStartOfToday() {
        return LocalDate.now(ZoneId.of("UTC")).atStartOfDay(ZoneId.of("UTC")).toInstant();
    }

    public static Instant getStartOfWeek() {
        return LocalDate.now(ZoneId.of("UTC"))
                .with(DayOfWeek.MONDAY)
                .atStartOfDay(ZoneId.of("UTC"))
                .toInstant();
    }

    public static Instant getStartOfMonth() {
        return LocalDate.now(ZoneId.of("UTC"))
                .withDayOfMonth(1)
                .atStartOfDay(ZoneId.of("UTC"))
                .toInstant();
    }

    public static Instant parseTimestamp(String timestamp) {
        if (timestamp == null || timestamp.isBlank()) {
            return Instant.now();
        }
        try {
            return Instant.parse(timestamp);
        } catch (Exception e) {
            return Instant.now();
        }
    }
}
