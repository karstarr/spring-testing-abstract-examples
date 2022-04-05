package com.griddynamics.akarsakov.services;

import com.griddynamics.akarsakov.entities.Rocket;
import com.griddynamics.akarsakov.entities.Satellite;
import com.griddynamics.akarsakov.repositories.RocketRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unitTest")
class RocketComposerServiceImplTest {

    private final RocketRepository repositoryMock = Mockito.mock(RocketRepository.class);
    private final RocketComposerServiceImpl service = new RocketComposerServiceImpl(repositoryMock);

    private Rocket rocket;
    private Satellite satellite;

    @BeforeEach
    void initData() {
        rocket = new Rocket(UUID.randomUUID(), "light");
        rocket.addParameter("delta-V", "1500");
        rocket.addParameter("Launch date", LocalDateTime.now().toString());

        satellite = new Satellite(UUID.randomUUID());
        rocket.addSatellite(satellite);
    }

    @AfterEach
    void cleanup() {
        satellite = null;
        rocket = null;
    }

    @Test
    void addLaunchParameter() {
        assertFalse(rocket.getParameters().containsKey("fuel type"));

        service.addLaunchParameter(rocket, "fuel type", "solid");

        assertTrue(rocket.getParameters().containsKey("fuel type"));
        assertEquals("solid", rocket.getParameters().get("fuel type"));
    }

    @Test
    void setLaunchParameters_parametersIsNull() {
        service.setLaunchParameters(rocket, null);

        assertTrue(rocket.getParameters().isEmpty());
    }

    @Test
    void setLaunchParameters_parametersIsEmpty() {
        service.setLaunchParameters(rocket, new HashMap<>());

        assertTrue(rocket.getParameters().isEmpty());
    }

    @Test
    void setLaunchParameters_notNullParameter() {
        service.setLaunchParameters(rocket, Map.of("delta-V", "1000"));

        assertEquals(2, rocket.getParameters().size());
        assertEquals("1000", rocket.getParameters().get("delta-V"));
    }

    @Test
    void setLaunchParameters_nullParameter() {
        Map<String, String> params = new HashMap<>();
        params.put("Launch date", null);
        service.setLaunchParameters(rocket, params);

        assertEquals(1, rocket.getParameters().size());
        assertNull(rocket.getParameters().get("Launch date"));
    }

    @Test
    void addSatellites_satellitesIsNull() {
        service.addSatellites(rocket, null);

        assertIterableEquals(Set.of(satellite), rocket.getSatellites());
    }

    @Test
    void addSatellites_satellitesIsEmpty() {
        service.addSatellites(rocket, new HashSet<>());

        assertIterableEquals(Set.of(satellite), rocket.getSatellites());
    }

    @Test
    void addSatellites_satelliteIsNull() {
        Set<Satellite> satellites = new HashSet<>();
        satellites.add(null);

        service.addSatellites(rocket, satellites);

        assertIterableEquals(Set.of(satellite), rocket.getSatellites());
    }

    @Test
    void addSatellites_addedSuccessful() {
        Set<Satellite> satellites = new HashSet<>();
        Satellite newSatellite = new Satellite(UUID.randomUUID());
        satellites.add(newSatellite);

        service.addSatellites(rocket, satellites);

        Comparator<Satellite> satelliteComparator = Comparator.comparingInt(Objects::hashCode);

        assertIterableEquals(Stream.of(satellite, newSatellite).sorted(satelliteComparator).toList(),
                rocket.getSatellites().stream().sorted(satelliteComparator).toList());
    }

    @Test
    void setSatellites_satellitesIsNull() {
        service.setSatellites(rocket, null);

        assertTrue(rocket.getSatellites().isEmpty());
    }

    @Test
    void setSatellites_setSuccessful() {
        Set<Satellite> satellites = new HashSet<>();
        Satellite newSatellite = new Satellite(UUID.randomUUID());
        satellites.add(newSatellite);

        service.setSatellites(rocket, satellites);

        assertIterableEquals(Set.of(newSatellite), rocket.getSatellites());
    }
}