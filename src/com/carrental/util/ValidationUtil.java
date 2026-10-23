package com.carrental.util;

public final class ValidationUtil {

    private ValidationUtil() {
        // Utility class
    }

    public static boolean isBlank(String value) {

        return value == null || value.isBlank();
    }

    public static boolean isValidEmail(String email) {

        if (isBlank(email)) {
            return false;
        }

        return email.matches(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
        );
    }

    public static boolean isValidPhone(String phone) {

        if (isBlank(phone)) {
            return false;
        }

        return phone.matches("\\d{10}");
    }

    public static boolean isPositive(double value) {

        return value > 0;
    }

    public static boolean isPositive(int value) {

        return value > 0;
    }

    public static boolean isValidDrivingLicence(
            String drivingLicence) {

        return !isBlank(drivingLicence)
                && drivingLicence.trim().length() >= 5;
    }

    public static String requireNonBlank(
            String value,
            String fieldName) {

        if (isBlank(value)) {
            throw new IllegalArgumentException(
                    fieldName + " cannot be empty."
            );
        }

        return value.trim();
    }
}