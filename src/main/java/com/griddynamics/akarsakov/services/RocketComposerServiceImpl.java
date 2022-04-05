package com.griddynamics.akarsakov.services;

import com.griddynamics.akarsakov.entities.Rocket;
import com.griddynamics.akarsakov.entities.Satellite;
import com.griddynamics.akarsakov.repositories.RocketRepository;
import com.griddynamics.akarsakov.services.search.SearchCondition;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RocketComposerServiceImpl implements RocketComposerService {
    private final RocketComposerServiceWorker worker;

    public RocketComposerServiceImpl(RocketRepository rocketRepository) {
        this.worker = new RocketComposerServiceWorker(rocketRepository);
    }

    @Override
    public Rocket getFirstRocketByConditions(List<SearchCondition> conditions) {
        return worker.searchByConditions(conditions);
    }

    @Override
    public void addLaunchParameter(@NonNull Rocket rocket, @NonNull String paramName, @NonNull String paramValue) {
        rocket.addParameter(paramName, paramValue);
    }

    @Override
    public void setLaunchParameters(@NonNull Rocket rocket, Map<String, String> launchParameters) {
        rocket.setParameters(launchParameters);
    }

    @Override
    public void addSatellites(@NonNull Rocket rocket, Set<Satellite> satellites) {
        if (satellites != null && !satellites.isEmpty()) {
            for (var satellite : satellites) {
                if (satellite != null) {
                    rocket.addSatellite(satellite);
                }
            }
        }
    }

    @Override
    public void setSatellites(@NonNull Rocket rocket, Set<Satellite> satellites) {
        rocket.setSatellites(satellites);
    }

}
