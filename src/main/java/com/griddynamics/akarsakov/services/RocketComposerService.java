package com.griddynamics.akarsakov.services;

import com.griddynamics.akarsakov.entities.Rocket;
import com.griddynamics.akarsakov.entities.Satellite;
import com.griddynamics.akarsakov.services.search.SearchCondition;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface RocketComposerService {

    Rocket getFirstRocketByConditions(List<SearchCondition> conditions);

    void addLaunchParameter(@NonNull Rocket rocket, @NonNull String paramName, @NonNull String paramValue);

    void setLaunchParameters(@NonNull Rocket rocket, Map<String, String> launchParameters);

    void addSatellites(@NonNull Rocket rocket, Set<Satellite> satellites);

    void setSatellites(@NonNull Rocket rocket, Set<Satellite> satellites);

}
