package com.griddynamics.akarsakov.services;

import com.griddynamics.akarsakov.entities.Rocket;
import com.griddynamics.akarsakov.entities.Spaceport;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@Service
public interface LauncherService {

    void deliverRocketToSpaceport(@NonNull Rocket rocket, @NonNull Spaceport spaceport);

    boolean launch(@NonNull Rocket rocket, @NonNull Spaceport spaceport);

}
