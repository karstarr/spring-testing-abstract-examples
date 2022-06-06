package com.griddynamics.akarsakov.utils;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unitTest")
class NumberUtilsTest {

    @Test
    void compareParamAndConditionValues_paramValue_IllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> NumberUtils.compareParamAndConditionValues("mx", "nil"));
    }

    @Test
    void compareParamAndConditionValues_value_IllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> NumberUtils.compareParamAndConditionValues("15.3", "nil"));
    }

    @Test
    void compareParamAndConditionValues_paramValue_lesserThan_value() {
        assertEquals(-1, NumberUtils.compareParamAndConditionValues("2.05", "2.06"));
    }

    @Test
    void isNumeric_number_successful() {
        assertTrue(NumberUtils.isNumeric("412f"));
    }

    @Test
    void isNumeric_null_successful() {
        assertTrue(NumberUtils.isNumeric(null));
    }

    @Test
    void isNumeric_unsuccessful() {
        assertFalse(NumberUtils.isNumeric("It is definitely not a number!111"));
    }

    @Test
    void isNumeric_unsuccessful_NaN() {
        assertFalse(NumberUtils.isNumeric("NaN"));
    }
}