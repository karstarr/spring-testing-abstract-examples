package com.griddynamics.akarsakov.utils;

import java.math.BigDecimal;

public final class NumberUtils {
    private NumberUtils() {}

    public static int compareParamAndConditionValues(String paramValue, String value) {
        String exceptionMessage = "Value '%s' is not a number";
        if (!isNumeric(paramValue)) {
            throw new IllegalArgumentException(exceptionMessage.formatted(paramValue));
        }
        if (!isNumeric(value)) {
            throw new IllegalArgumentException(exceptionMessage.formatted(value));
        }

        return new BigDecimal(paramValue).compareTo(new BigDecimal(value));
    }

    public static boolean isNumeric(String valueToCheck) {
        if (valueToCheck == null) {
            return true;
        }
        try {
            Double.parseDouble(valueToCheck.toLowerCase());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
