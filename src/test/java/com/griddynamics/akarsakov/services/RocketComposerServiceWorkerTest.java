package com.griddynamics.akarsakov.services;

import com.griddynamics.akarsakov.entities.Rocket;
import com.griddynamics.akarsakov.entities.Satellite;
import com.griddynamics.akarsakov.entities.Spaceport;
import com.griddynamics.akarsakov.repositories.RocketRepository;
import com.griddynamics.akarsakov.services.search.SearchCondition;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.*;

import static com.griddynamics.akarsakov.services.search.SearchCondition.Condition.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Tag("unitTest")
class RocketComposerServiceWorkerTest {

    private final RocketRepository mockRepository = Mockito.mock(RocketRepository.class);
    private final RocketComposerServiceWorker worker = new RocketComposerServiceWorker(mockRepository);

    private static List<Rocket> rockets;

    @BeforeAll
    static void init() {
        rockets = List.of(
                new Rocket(UUID.randomUUID(), "heavy"),
                new Rocket(UUID.randomUUID(), "heavy"),
                new Rocket(UUID.randomUUID(), "super-heavy"),
                new Rocket(UUID.randomUUID(), "light")
        );

        rockets.get(0).setMissionName("GPS renew");
        rockets.get(1).setMissionName("GPS renew");
        rockets.get(2).setMissionName("Juno");

        Satellite satellite = new Satellite(UUID.randomUUID());
        satellite.setAssignment("far space exploration");
        rockets.get(0).addSatellite(satellite);

        Satellite juno = new Satellite(UUID.randomUUID());
        juno.setName("Juno");
        juno.setAssignment("research of far space magnetic fields");
        rockets.get(2).addSatellite(juno);

        Spaceport spaceport = new Spaceport(UUID.randomUUID(), 0.0, 15.3);
        rockets.get(2).setSpaceport(spaceport);

        rockets.get(0).addParameter("delta-V", "2000");
        rockets.get(1).addParameter("delta-V", "1700");
        rockets.get(2).addParameter("delta-V", "4500");
        rockets.get(2).addParameter("purpose", "study of Jupiter and its closest moons");
        rockets.get(3).addParameter("delta-V", "900");
    }

    @Test
    void searchByConditions_nullConditionsList() {
        assertNull(worker.searchByConditions(null));
    }

    @Test
    void searchByConditions_emptyConditionsList() {
        assertNull(worker.searchByConditions(new ArrayList<>()));
    }

    @Test
    void findRocketWithIdInConditions_rocketId_default_case() {
        List<SearchCondition> conditions = List.of(
                new SearchCondition("id", LESSER_THAN, UUID.randomUUID())
        );
        assertThrows(IllegalArgumentException.class, () -> worker.searchByConditions(conditions));
    }

    @Test
    void findRocketWithIdInConditions_rocketId_NOT_EQUALS_case() {
        List<SearchCondition> conditions = List.of(
                new SearchCondition("id", NOT_EQUALS, UUID.randomUUID()),
                new SearchCondition("delta-V", GREATER_OR_EQUALS_THAN, 2200)
        );

        when(mockRepository.findByIdNotIn(any())).thenReturn(rockets);

        assertEquals(rockets.get(2), worker.searchByConditions(conditions));
    }

    @Test
    void findRocketWithIdInConditions_rocketId_EQUALS_case() {
        Rocket lightOne = rockets.get(3);

        List<SearchCondition> conditions = List.of(
                new SearchCondition("id", EQUALS, lightOne.getId()),
                new SearchCondition("delta-V", LESSER_OR_EQUALS_THAN, 1900)
        );

        when(mockRepository.findById(lightOne.getId())).thenReturn(Optional.of(lightOne));

        assertEquals(lightOne, worker.searchByConditions(conditions));
    }

    @Test
    void findRocketWithoutIdInConditions_noRocketIdInConditions() {
        List<SearchCondition> conditions = List.of(
                new SearchCondition("delta-V", GREATER_OR_EQUALS_THAN, 2000)
        );

        when(mockRepository.findAll()).thenReturn(rockets);

        assertEquals(rockets.get(0), worker.searchByConditions(conditions));
    }

    @Test
    void findRockets_getAll() {
        when(mockRepository.findAll()).thenReturn(rockets);

        List<SearchCondition> conditions = List.of(new SearchCondition("delta-V", GREATER_THAN, 2500));

        assertEquals(rockets.get(2), worker.searchByConditions(conditions));
    }

    @Test
    void findRockets_allMainConditionsAreSet() {
        List<Rocket> foundRockets = List.of(rockets.get(0), rockets.get(1));

        when(mockRepository.findByTypeAndMissionNameRegexes(any(), any())).thenReturn(foundRockets);

        List<SearchCondition> conditions = List.of(
                new SearchCondition("type", EQUALS, "heavy"),
                new SearchCondition("missionName", LIKE, "gps")
        );

        assertEquals(rockets.get(0), worker.searchByConditions(conditions));
    }

    @Test
    void findRockets_onlyTypeConditionSet() {
        List<Rocket> foundRockets = List.of(rockets.get(0), rockets.get(1));

        when(mockRepository.findByTypeRegex(any())).thenReturn(foundRockets);

        List<SearchCondition> conditions = List.of(
                new SearchCondition("type", EQUALS, "heavy")
        );

        assertEquals(rockets.get(0), worker.searchByConditions(conditions));
    }

    @Test
    void findRockets_onlyMissionNameConditionSet() {
        List<Rocket> foundRockets = List.of(rockets.get(0), rockets.get(1));

        when(mockRepository.findByMissionNameRegex(any())).thenReturn(foundRockets);

        List<SearchCondition> conditions = List.of(
                new SearchCondition("missionName", LIKE, "gps")
        );

        assertEquals(rockets.get(0), worker.searchByConditions(conditions));
    }

    @Test
    void checkCondition_nullRocket() {
        List<SearchCondition> conditions = List.of(
                new SearchCondition("attrName", EQUALS, "1")
        );

        List<Rocket> foundRockets = new ArrayList<>(1);
        foundRockets.add(null);

        when(mockRepository.findAll()).thenReturn(foundRockets);

        assertNull(worker.searchByConditions(conditions));
    }

    @Test
    void checkCondition_nullCondition() {
        List<SearchCondition> conditions = new ArrayList<>(1);
        conditions.add(null);

        when(mockRepository.findAll()).thenReturn(rockets);

        assertEquals(rockets.get(0), worker.searchByConditions(conditions));
    }

    @Test
    void checkCondition_type() {
        List<SearchCondition> conditions = List.of(
                new SearchCondition("id", NOT_EQUALS, rockets.get(0).getId()),
                new SearchCondition("type", EQUALS, "light")
        );

        List<Rocket> foundRockets = List.of(rockets.get(1), rockets.get(2), rockets.get(3));

        when(mockRepository.findByIdNotIn(any())).thenReturn(foundRockets);

        assertEquals(rockets.get(3), worker.searchByConditions(conditions));
    }

    @Test
    void checkCondition_missionName() {
        List<SearchCondition> conditions = List.of(
                new SearchCondition("id", NOT_EQUALS, rockets.get(0).getId()),
                new SearchCondition("missionName", EQUALS, "Juno")
        );

        List<Rocket> foundRockets = List.of(rockets.get(1), rockets.get(2), rockets.get(3));

        when(mockRepository.findByIdNotIn(any())).thenReturn(foundRockets);

        assertEquals(rockets.get(2), worker.searchByConditions(conditions));
    }

    @Test
    void checkCondition_spaceport() {
        SearchCondition condition = new SearchCondition("spaceport", LIKE, "\"longitude\" : 0.0");

        when(mockRepository.findAll()).thenReturn(rockets);

        assertEquals(rockets.get(2), worker.searchByConditions(List.of(condition)));
    }

    @Test
    void checkCondition_noSatellites() {
        List<SearchCondition> conditions = List.of(new SearchCondition("attrName", EQUALS, "1"));

        when(mockRepository.findAll()).thenReturn(rockets);

        assertNull(worker.searchByConditions(conditions));
    }

    @Test
    void checkCondition_failureOnSatellite() {
        List<SearchCondition> conditions = List.of(
                new SearchCondition("id", EQUALS, rockets.get(0).getId()),
                new SearchCondition("satellite.assignment", EQUALS, "navigation probe")
        );

        when(mockRepository.findById(any())).thenReturn(Optional.ofNullable(rockets.get(0)));

        assertNull(worker.searchByConditions(conditions));
    }

    @Test
    void checkParameter_numericConditionWithNonNumericValue() {
        assertThrowsExactly(
                IllegalArgumentException.class,
                () -> {
                    SearchCondition condition = new SearchCondition("delta-V", LESSER_THAN, "NaN");

                    when(mockRepository.findAll()).thenReturn(rockets);

                    worker.searchByConditions(List.of(condition));
                });
    }

    @Test
    void checkParameter_failed_NOT_EQUALS_condition() {
        SearchCondition condition = new SearchCondition("delta-V", NOT_EQUALS, 900);

        when(mockRepository.findAll()).thenReturn(List.of(rockets.get(3)));

        assertNull(worker.searchByConditions(List.of(condition)));
    }

    @Test
    void checkParameter_successful_NOT_EQUALS_condition() {
        SearchCondition condition = new SearchCondition("delta-V", NOT_EQUALS, 700);

        when(mockRepository.findAll()).thenReturn(List.of(rockets.get(3)));

        assertEquals(rockets.get(3), worker.searchByConditions(List.of(condition)));
    }

    @Test
    void checkParameter_successful_EQUALS_condition() {
        SearchCondition condition = new SearchCondition("delta-V", EQUALS, 1700);

        when(mockRepository.findAll()).thenReturn(rockets);

        assertEquals(rockets.get(1), worker.searchByConditions(List.of(condition)));
    }

    @Test
    void checkParameter_successful_LIKE_condition() {
        SearchCondition condition = new SearchCondition("purpose", LIKE, "Jupiter");

        when(mockRepository.findAll()).thenReturn(rockets);

        assertEquals(rockets.get(2), worker.searchByConditions(List.of(condition)));
    }

    @Test
    void checkParameter_successful_LESSER_THAN_condition() {
        SearchCondition condition = new SearchCondition("delta-V", LESSER_THAN, -1.001);

        when(mockRepository.findAll()).thenReturn(rockets);

        assertNull(worker.searchByConditions(List.of(condition)));
    }

    @Test
    void checkParameter_successful_GREATER_THAN_condition() {
        SearchCondition condition = new SearchCondition("delta-V", GREATER_THAN, 2000);

        when(mockRepository.findAll()).thenReturn(rockets);

        assertEquals(rockets.get(2), worker.searchByConditions(List.of(condition)));
    }

    @Test
    void checkParameter_successful_LESSER_OR_EQUALS_THAN_condition() {
        SearchCondition condition = new SearchCondition("delta-V", LESSER_OR_EQUALS_THAN, 1699.999);

        when(mockRepository.findAll()).thenReturn(rockets);

        assertEquals(rockets.get(3), worker.searchByConditions(List.of(condition)));
    }

    @Test
    void checkParameter_successful_GREATER_OR_EQUALS_THAN_condition() {
        SearchCondition condition = new SearchCondition("delta-V", GREATER_OR_EQUALS_THAN, 1700.0001);

        when(mockRepository.findAll()).thenReturn(rockets);

        assertEquals(rockets.get(0), worker.searchByConditions(List.of(condition)));
    }

    @Test
    void checkSatellite_noSatelliteConditions() {
        SearchCondition condition = new SearchCondition("rocketParam", NOT_EQUALS, "some value");

        when(mockRepository.findAll()).thenReturn(List.of(rockets.get(0), rockets.get(2)));

        assertNull(worker.searchByConditions(List.of(condition)));
    }

    @Test
    void checkSatellite_satelliteName_NOT_EQUALS() {
        SearchCondition condition = new SearchCondition("satellite.name", NOT_EQUALS, "Voyager 2");

        when(mockRepository.findAll()).thenReturn(rockets);

        assertEquals(rockets.get(0), worker.searchByConditions(List.of(condition)));
    }

    @Test
    void checkSatellite_satelliteName_EQUALS() {
        SearchCondition condition = new SearchCondition("satellite.name", EQUALS, "Juno");

        when(mockRepository.findAll()).thenReturn(rockets);

        assertEquals(rockets.get(2), worker.searchByConditions(List.of(condition)));
    }

    @Test
    void checkSatellite_satelliteName_LIKE() {
        SearchCondition condition = new SearchCondition("satellite.name", LIKE, "Jun");

        when(mockRepository.findAll()).thenReturn(rockets);

        assertEquals(rockets.get(2), worker.searchByConditions(List.of(condition)));
    }

    @Test
    void checkSatellite_satelliteName_default() {
        SearchCondition condition = new SearchCondition("satellite.name", LESSER_THAN, 214);

        when(mockRepository.findAll()).thenReturn(rockets);

        assertNull(worker.searchByConditions(List.of(condition)));
    }

    @Test
    void checkSatellite_satelliteAssignment_NOT_EQUALS() {
        SearchCondition condition = new SearchCondition("satellite.assignment",
                NOT_EQUALS,
                "far space exploration");

        when(mockRepository.findAll()).thenReturn(rockets);

        assertEquals(rockets.get(2), worker.searchByConditions(List.of(condition)));
    }

    @Test
    void checkSatellite_satelliteAssignment_EQUALS() {
        SearchCondition condition = new SearchCondition("satellite.assignment",
                EQUALS,
                "far space exploration");

        when(mockRepository.findAll()).thenReturn(rockets);

        assertEquals(rockets.get(0), worker.searchByConditions(List.of(condition)));
    }

    @Test
    void checkSatellite_satelliteAssignment_LIKE() {
        SearchCondition condition = new SearchCondition("satellite.assignment",
                LIKE, "far space");

        when(mockRepository.findAll()).thenReturn(rockets);

        assertEquals(rockets.get(0), worker.searchByConditions(List.of(condition)));
    }

    @Test
    void checkSatellite_satelliteAssignment_default() {
        SearchCondition condition = new SearchCondition("satellite.assignment", LESSER_THAN, 3);

        when(mockRepository.findAll()).thenReturn(rockets);

        assertNull(worker.searchByConditions(List.of(condition)));
    }
}