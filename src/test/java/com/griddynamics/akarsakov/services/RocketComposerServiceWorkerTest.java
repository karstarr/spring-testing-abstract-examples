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
import java.util.function.Predicate;

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
    void searchByConditions_rocketId_default_case() {
        List<SearchCondition> conditions = List.of(
                new SearchCondition("id", LESSER_THAN, UUID.randomUUID())
        );
        assertThrows(IllegalArgumentException.class, () -> worker.searchByConditions(conditions));
    }

    @Test
    void searchByConditions_noRocketIdInConditions() {
        List<SearchCondition> conditions = List.of(
                new SearchCondition("delta-V", GREATER_OR_EQUALS_THAN, 2000)
        );

        when(mockRepository.findAll()).thenReturn(rockets);

        assertEquals(rockets.get(0), worker.searchByConditions(conditions));
    }

    @Test
    void searchByConditions_rocketId_NOT_EQUALS_case() {
        List<SearchCondition> conditions = List.of(
                new SearchCondition("id", NOT_EQUALS, UUID.randomUUID()),
                new SearchCondition("delta-V", GREATER_OR_EQUALS_THAN, 2200)
        );

        when(mockRepository.findByIdNotIn(any())).thenReturn(rockets);

        assertEquals(rockets.get(2), worker.searchByConditions(conditions));
    }

    @Test
    void searchByConditions_rocketId_EQUALS_case() {
        Rocket lightOne = rockets.get(3);

        List<SearchCondition> conditions = List.of(
                new SearchCondition("id", EQUALS, lightOne.getId()),
                new SearchCondition("delta-V", LESSER_OR_EQUALS_THAN, 1900)
        );

        when(mockRepository.findById(lightOne.getId())).thenReturn(Optional.of(lightOne));

        assertEquals(lightOne, worker.searchByConditions(conditions));
    }

    @Test
    void extractRocketId_nullConditions() {
        Predicate<SearchCondition> hasId = (condition) -> condition.isAttributeNameSimilar("id");
        assertEquals(new SearchCondition(null, null, null),
                worker.extractRocketIdCondition(null, hasId));
    }

    @Test
    void extractRocketId_nullPredicate() {
        List<SearchCondition> conditions = List.of(new SearchCondition("id", EQUALS, UUID.randomUUID()));
        assertEquals(new SearchCondition(null, null, null),
                worker.extractRocketIdCondition(conditions, null));
    }

    @Test
    void extractRocketId_successful() {
        List<SearchCondition> conditions = List.of(
                new SearchCondition("id", EQUALS, UUID.randomUUID()),
                new SearchCondition("type", LIKE, "light"),
                new SearchCondition("spaceport",
                        SearchCondition.Condition.NOT_EQUALS,
                        new Spaceport(UUID.randomUUID(), -40.0, 20.0))
        );

        Predicate<SearchCondition> hasId = (condition) -> condition.isAttributeNameSimilar("id");

        assertEquals(conditions.get(0), worker.extractRocketIdCondition(conditions, hasId));
    }

    @Test
    void extractRocketId_unsuccessful() {
        List<SearchCondition> conditions = List.of(
                new SearchCondition("satellite", EQUALS, UUID.randomUUID()),
                new SearchCondition("type", LIKE, "light"),
                new SearchCondition("spaceport",
                        NOT_EQUALS,
                        new Spaceport(UUID.randomUUID(), -40.0, 20.0))
        );

        Predicate<SearchCondition> hasId = (condition) -> condition.isAttributeNameSimilar("id");

        assertEquals(new SearchCondition(null, null, null),
                worker.extractRocketIdCondition(conditions, hasId));
    }

    @Test
    void filterRocket_rocketNull() {
        assertNull(worker.filterRocket(null, new ArrayList<>()));
    }

    @Test
    void filterRocket_conditionsNull() {
        Rocket rocket = rockets.get(3);

        assertEquals(rocket, worker.filterRocket(rocket, null));
    }

    @Test
    void filterRocket_conditionsEmpty() {
        Rocket rocket = rockets.get(3);

        assertEquals(rocket, worker.filterRocket(rocket, new ArrayList<>()));
    }

    @Test
    void filterRocket_conditionsMatched() {
        Rocket rocket = rockets.get(3);

        List<SearchCondition> conditions = List.of(
                new SearchCondition("type", EQUALS, "light"),
                new SearchCondition("delta-V", GREATER_THAN, 850)
        );

        assertEquals(rocket, worker.filterRocket(rocket, conditions));
    }

    @Test
    void filterRocket_conditionNotMatched() {
        List<SearchCondition> conditions = List.of(
                new SearchCondition("type", EQUALS, "light"),
                new SearchCondition("delta-V", LESSER_OR_EQUALS_THAN, 850)
        );

        assertNull(worker.filterRocket(rockets.get(3), conditions));
    }

    @Test
    void findRockets_getAll() {
        Predicate<SearchCondition> hasType = (condition) -> condition.isAttributeNameSimilar("type");
        Predicate<SearchCondition> hasMissionName = (condition) -> condition.isAttributeNameSimilar("missionName");

        when(mockRepository.findAll()).thenReturn(rockets);

        List<SearchCondition> conditions = List.of(new SearchCondition("max thrust", GREATER_THAN, 2500));

        assertIterableEquals(rockets, worker.findRockets(conditions, hasType, hasMissionName));
    }

    @Test
    void findRockets_conditionsListIsNull() {
        Predicate<SearchCondition> hasType = (condition) -> condition.isAttributeNameSimilar("type");
        Predicate<SearchCondition> hasMissionName = (condition) -> condition.isAttributeNameSimilar("missionName");

        List<Rocket> rockets = new LinkedList<>();

        assertIterableEquals(rockets, worker.findRockets(null, hasType, hasMissionName));
    }

    @Test
    void findRockets_ConditionsListIsEmpty() {
        Predicate<SearchCondition> hasType = (condition) -> condition.isAttributeNameSimilar("type");
        Predicate<SearchCondition> hasMissionName = (condition) -> condition.isAttributeNameSimilar("missionName");

        when(mockRepository.findAll()).thenReturn(rockets);

        List<SearchCondition> conditions = new ArrayList<>();

        assertIterableEquals(rockets, worker.findRockets(conditions, hasType, hasMissionName));
    }

    @Test
    void findRockets_allMainConditionsAreSet() {
        Predicate<SearchCondition> hasType = (condition) -> condition.isAttributeNameSimilar("type");
        Predicate<SearchCondition> hasMissionName = (condition) -> condition.isAttributeNameSimilar("missionName");

        List<Rocket> foundRockets = List.of(rockets.get(0), rockets.get(1));

        when(mockRepository.findByTypeAndMissionNameRegexes(any(), any())).thenReturn(foundRockets);

        List<SearchCondition> conditions = List.of(
                new SearchCondition("type", EQUALS, "heavy"),
                new SearchCondition("missionName", LIKE, "gps")
        );

        assertIterableEquals(foundRockets, worker.findRockets(conditions, hasType, hasMissionName));
    }

    @Test
    void findRockets_onlyTypeConditionSet() {
        Predicate<SearchCondition> hasType = (condition) -> condition.isAttributeNameSimilar("type");
        Predicate<SearchCondition> hasMissionName = (condition) -> condition.isAttributeNameSimilar("missionName");

        List<Rocket> foundRockets = List.of(rockets.get(0), rockets.get(1));

        when(mockRepository.findByTypeRegex(any())).thenReturn(foundRockets);

        List<SearchCondition> conditions = List.of(
                new SearchCondition("type", EQUALS, "heavy")
        );

        assertIterableEquals(foundRockets, worker.findRockets(conditions, hasType, hasMissionName));
    }

    @Test
    void findRockets_onlyMissionNameConditionSet() {
        Predicate<SearchCondition> hasType = (condition) -> condition.isAttributeNameSimilar("type");
        Predicate<SearchCondition> hasMissionName = (condition) -> condition.isAttributeNameSimilar("missionName");

        List<Rocket> foundRockets = List.of(rockets.get(0), rockets.get(1));

        when(mockRepository.findByMissionNameRegex(any())).thenReturn(foundRockets);

        List<SearchCondition> conditions = List.of(
                new SearchCondition("missionName", LIKE, "gps")
        );

        assertIterableEquals(foundRockets, worker.findRockets(conditions, hasType, hasMissionName));
    }

    @Test
    void filterRockets_noRocketsToFilter() {
        assertNull(worker.filterRockets(null, new ArrayList<>()));
    }

    @Test
    void filterRockets_noneLeftAfterFiltering() {
        List<SearchCondition> conditions = List.of(
                new SearchCondition("type", EQUALS, "heavy"),
                new SearchCondition("delta-V", GREATER_THAN, 4500)
        );

        assertNull(worker.filterRockets(rockets, conditions));
    }

    @Test
    void filterRockets_someLeftAfterFiltering() {
        List<SearchCondition> conditions = List.of(new SearchCondition("type", EQUALS, "heavy"));

        assertEquals(rockets.get(0), worker.filterRockets(rockets, conditions));
    }

    @Test
    void filterRockets_oneLeftAfterFiltering() {
        List<SearchCondition> conditions = List.of(new SearchCondition("type", EQUALS, "super-heavy"));

        assertEquals(rockets.get(2), worker.filterRockets(rockets, conditions));
    }

    @Test
    void checkCondition_nullRocket() {
        SearchCondition condition = new SearchCondition("attrName", EQUALS, "1");

        assertFalse(worker.checkCondition(null, condition));
    }

    @Test
    void checkCondition_nullCondition() {
        assertTrue(worker.checkCondition(rockets.get(3), null));
    }

    @Test
    void checkCondition_type() {
        SearchCondition condition = new SearchCondition("type", EQUALS, "light");

        assertTrue(worker.checkCondition(rockets.get(3), condition));
    }

    @Test
    void checkCondition_missionName() {
        SearchCondition condition = new SearchCondition("missionName", EQUALS, "Juno");

        assertTrue(worker.checkCondition(rockets.get(2), condition));
    }

    @Test
    void checkCondition_spaceport() {
        SearchCondition condition = new SearchCondition("spaceport", LIKE, "\"longitude\" : 0.0");

        assertTrue(worker.checkCondition(rockets.get(2), condition));
    }

    @Test
    void checkCondition_deltaVParameter() {
        SearchCondition condition = new SearchCondition("delta-V", EQUALS, "1700");

        assertTrue(worker.checkCondition(rockets.get(1), condition));
    }

    @Test
    void checkCondition_noSatellites() {
        SearchCondition condition = new SearchCondition("attrName", EQUALS, "1");

        assertFalse(worker.checkCondition(rockets.get(3), condition));
    }

    @Test
    void checkCondition_failureOnSatellite() {
        SearchCondition condition = new SearchCondition("satellite.assignment", EQUALS, "navigation probe");

        assertFalse(worker.checkCondition(rockets.get(0), condition));
    }

    @Test
    void checkParameter_numericConditionWithNonNumericValue() {
        assertThrowsExactly(
                IllegalArgumentException.class,
                () -> {
                    SearchCondition condition = new SearchCondition("attrName", LESSER_THAN, "NaN");
                    worker.checkParameter("parameter", condition);
                });
    }

    @Test
    void checkParameter_failed_NOT_EQUALS_condition() {
        SearchCondition condition = new SearchCondition("param", NOT_EQUALS, 15);

        assertFalse(worker.checkParameter("15", condition));
    }

    @Test
    void checkParameter_successful_NOT_EQUALS_condition() {
        SearchCondition condition = new SearchCondition("param", NOT_EQUALS, 15);

        assertTrue(worker.checkParameter("pineapple", condition));
    }

    @Test
    void checkParameter_successful_EQUALS_condition() {
        SearchCondition condition = new SearchCondition("param", EQUALS, 15);

        assertTrue(worker.checkParameter("15", condition));
    }

    @Test
    void checkParameter_successful_LIKE_condition() {
        SearchCondition condition = new SearchCondition("param", LIKE, 15);

        assertTrue(worker.checkParameter("there are 15 grapes", condition));
    }

    @Test
    void checkParameter_successful_LESSER_THAN_condition() {
        SearchCondition condition = new SearchCondition("param", LESSER_THAN, 15);

        assertTrue(worker.checkParameter("-150.1", condition));
    }

    @Test
    void checkParameter_successful_GREATER_THAN_condition() {
        SearchCondition condition = new SearchCondition("param", GREATER_THAN, 15);

        assertTrue(worker.checkParameter("15.01", condition));
    }

    @Test
    void checkParameter_successful_LESSER_OR_EQUALS_THAN_condition() {
        SearchCondition condition = new SearchCondition("param", LESSER_OR_EQUALS_THAN, 15);

        assertTrue(worker.checkParameter("14.999", condition));
        assertTrue(worker.checkParameter("15", condition));
    }

    @Test
    void checkParameter_successful_GREATER_OR_EQUALS_THAN_condition() {
        SearchCondition condition = new SearchCondition("param", GREATER_OR_EQUALS_THAN, 15);

        assertTrue(worker.checkParameter("15", condition));
        assertTrue(worker.checkParameter("15.0001", condition));
    }

    @Test
    void checkSatellite_noSatelliteConditions() {
        SearchCondition condition = new SearchCondition("rocketParam", NOT_EQUALS, "some value");

        assertFalse(worker.checkSatellite(null, condition));
    }

    @Test
    void checkSatellite_satelliteName_NOT_EQUALS() {
        SearchCondition condition = new SearchCondition("satellite.name", NOT_EQUALS, "Voyager 2");

        assertTrue(worker.checkSatellite(rockets.get(2), condition));
    }

    @Test
    void checkSatellite_satelliteName_EQUALS() {
        SearchCondition condition = new SearchCondition("satellite.name", EQUALS, "Juno");

        assertTrue(worker.checkSatellite(rockets.get(2), condition));
    }

    @Test
    void checkSatellite_satelliteName_LIKE() {
        SearchCondition condition = new SearchCondition("satellite.name", LIKE, "Jun");

        assertTrue(worker.checkSatellite(rockets.get(2), condition));
    }

    @Test
    void checkSatellite_satelliteName_default() {
        SearchCondition condition = new SearchCondition("satellite.name", LESSER_THAN, 214);

        assertFalse(worker.checkSatellite(rockets.get(2), condition));
    }

    @Test
    void checkSatellite_satelliteAssignment_NOT_EQUALS() {
        SearchCondition condition = new SearchCondition("satellite.assignment",
                NOT_EQUALS,
                "far space exploration");

        assertTrue(worker.checkSatellite(rockets.get(2), condition));
    }

    @Test
    void checkSatellite_satelliteAssignment_EQUALS() {
        SearchCondition condition = new SearchCondition("satellite.assignment",
                EQUALS,
                "far space exploration");

        assertTrue(worker.checkSatellite(rockets.get(0), condition));
    }

    @Test
    void checkSatellite_satelliteAssignment_LIKE() {
        SearchCondition condition = new SearchCondition("satellite.assignment",
                LIKE, "far space");

        assertTrue(worker.checkSatellite(rockets.get(2), condition));
    }

    @Test
    void checkSatellite_satelliteAssignment_default() {
        SearchCondition condition = new SearchCondition("satellite.assignment", LESSER_THAN, 3);

        assertFalse(worker.checkSatellite(rockets.get(2), condition));
    }

    @Test
    void isNumeric_number_successful() {
        assertTrue(RocketComposerServiceWorker.isNumeric("412f"));
    }

    @Test
    void isNumeric_null_successful() {
        assertTrue(RocketComposerServiceWorker.isNumeric(null));
    }

    @Test
    void isNumeric_unsuccessful() {
        assertFalse(RocketComposerServiceWorker.isNumeric("It is definitely not a number!111"));
    }

    @Test
    void isNumeric_unsuccessful_NaN() {
        assertFalse(RocketComposerServiceWorker.isNumeric("NaN"));
    }

    @Test
    void sanitizeConditions_conditionsListIsNull() {
        List<Predicate<SearchCondition>> excludePredicates = List.of(
                (condition) -> condition.isAttributeNameSimilar("id")
        );
        assertTrue(worker.sanitizeConditions(null, excludePredicates).isEmpty());
    }

    @Test
    void sanitizeConditions_predicatesListIsNull() {
        List<SearchCondition> conditions = List.of(
                new SearchCondition("satellite.assignment", LESSER_THAN, 3)
        );
        assertEquals(conditions, worker.sanitizeConditions(conditions, null));
    }

    @Test
    void sanitizeConditions_noneLeftAfterFiltering() {
        List<Predicate<SearchCondition>> excludePredicates = List.of(
                (condition) -> condition.isAttributeNameSimilar("id"),
                (condition) -> condition.isAttributeNameSimilar("satellite")
        );
        List<SearchCondition> conditions = List.of(
                new SearchCondition("id", EQUALS, 3),
                new SearchCondition("satellite", NOT_EQUALS, "g-415")
        );

        assertTrue(worker.sanitizeConditions(conditions, excludePredicates).isEmpty());
    }

    @Test
    void sanitizeConditions_someLeftAfterFiltering() {
        List<Predicate<SearchCondition>> excludePredicates = List.of(
                (condition) -> condition.isAttributeNameSimilar("satellite")
        );
        List<SearchCondition> conditions = List.of(
                new SearchCondition("id", EQUALS, 3),
                new SearchCondition("satellite", NOT_EQUALS, "g-415")
        );

        assertIterableEquals(List.of(conditions.get(0)), worker.sanitizeConditions(conditions, excludePredicates));
    }
}