package com.griddynamics.akarsakov.services;

import com.griddynamics.akarsakov.entities.Rocket;
import com.griddynamics.akarsakov.entities.Spaceport;
import com.griddynamics.akarsakov.utils.ChanceCalculator;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class LauncherServiceImpl implements LauncherService {

    private final ChanceCalculator calculator;

    public LauncherServiceImpl(ChanceCalculator calculator) {
        this.calculator = calculator;
    }

    @Override
    public void deliverRocketToSpaceport(@NonNull Rocket rocket, @NonNull Spaceport spaceport) {
        rocket.setSpaceport(spaceport);
    }

    @Override
    public boolean launch(@NonNull Rocket rocket, @NonNull Spaceport spaceport) {
        rocket.setSpaceport(spaceport);
        LocalDateTime launchDateTime = LocalDateTime.now();
        rocket.addParameter("Launch date", launchDateTime.toString());
        rocket.addParameter("Fuel", "100%");

        int failureChance = 10;

        boolean isLaunchSuccessful = calculator.rollForLaunchSuccess() > failureChance;

        rocket.addParameter("Success", Boolean.toString(isLaunchSuccessful));

        return isLaunchSuccessful;
    }
}
