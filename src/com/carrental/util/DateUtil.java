package com.carrental.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

public final class DateUtil {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ISO_LOCAL_DATE;

    private DateUtil() {
        // Utility class
    }

    public static LocalDate parseDate(String date) {

        if (date == null || date.isBlank()) {
            throw new IllegalArgumentException(
                    "Date cannot be empty."
            );
        }

        try {
            return LocalDate.parse(date, FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Invalid date format. Use YYYY-MM-DD."
            );
        }
    }

    public static String formatDate(LocalDate date) {

        if (date == null) {
            return "";
        }

        return date.format(FORMATTER);
    }

    public static long calculateDays(
            LocalDate startDate,
            LocalDate endDate) {

        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException(
                    "Start date and end date are required."
            );
        }

        return ChronoUnit.DAYS.between(
                startDate,
                endDate
        );
    }

    public static boolean isPast(LocalDate date) {

        return date != null
                && date.isBefore(LocalDate.now());
    }

    public static boolean isValidRentalPeriod(
            LocalDate startDate,
            LocalDate endDate) {

        if (startDate == null || endDate == null) {
            return false;
        }

        if (startDate.isBefore(LocalDate.now())) {
            return false;
        }

        if (endDate.isBefore(startDate)) {
            return false;
        }

        return calculateDays(startDate, endDate) >= 1;
    }
}
