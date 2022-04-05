package com.griddynamics.akarsakov.utils;

import java.util.Random;

public class ChanceCalculator {

    public int rollForLaunchSuccess() {
        return new Random().nextInt(100);
    }

}
