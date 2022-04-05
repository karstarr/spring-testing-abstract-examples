package com.griddynamics.akarsakov.utils;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unitTest")
class ChanceCalculatorTest {

    @Test
    void launchSuccessValueBetweenZeroAndOneHundred() {
        ChanceCalculator calculator = new ChanceCalculator();
        int actualChance = calculator.rollForLaunchSuccess();
        assertTrue(actualChance >= 0 && actualChance <= 100);
    }
}