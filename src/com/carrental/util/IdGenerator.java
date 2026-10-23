package com.carrental.util;

import java.util.UUID;

public final class IdGenerator {

    private IdGenerator() {
        // Utility class
    }

    public static String generateCarId() {
        return generateId("CAR");
    }

    public static String generateCustomerId() {
        return generateId("CUS");
    }

    public static String generateBookingId() {
        return generateId("BOOK");
    }

    private static String generateId(String prefix) {

        String uuidPart = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 8)
                .toUpperCase();

        return prefix + "-" + uuidPart;
    }
}
