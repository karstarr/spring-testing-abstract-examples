package com.griddynamics.akarsakov.repositories;

import com.griddynamics.akarsakov.TestingSpringBootApplication;
import com.griddynamics.akarsakov.entities.Rocket;
import com.griddynamics.akarsakov.services.search.SearchCondition;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static com.griddynamics.akarsakov.utils.TextSearchConditionsTranslator.buildSearchRegex;

@Tag("integration")
@DataMongoTest
@ExtendWith(SpringExtension.class)
@DirtiesContext
@ContextConfiguration(classes = {TestingSpringBootApplication.class})
class RocketRepositoryIntegrationTest {
    @Autowired
    private RocketRepository repository;

    private static List<Rocket> rockets;

    @BeforeAll
    static void initDatabase(@Autowired MongoTemplate mongoTemplate) {
        rockets = List.of(
                new Rocket(UUID.randomUUID(), "light"),
                new Rocket(UUID.randomUUID(), "heavy"),
                new Rocket(UUID.randomUUID(), "super-heavy")
        );
        rockets.get(0).setMissionName("GPS grid update");
        rockets.get(1).setMissionName("L1 telescope deploy");
        rockets.get(2).setMissionName("Jupiter L2 communication probe deploy");

        rockets.forEach(mongoTemplate::save);
    }

    @AfterAll
    static void cleanupDatabase(@Autowired MongoTemplate mongoTemplate) {
        rockets.forEach(mongoTemplate::remove);
    }

    @Test
    void findByIdNotIn() {
        assertIterableEquals(
                List.of(rockets.get(1)),
                repository.findByIdNotIn(List.of(rockets.get(0).getId(), rockets.get(2).getId()))
        );
    }

    @Test
    void findByTypeRegex() {
        SearchCondition condition = new SearchCondition("type", SearchCondition.Condition.LIKE, "heav");

        assertIterableEquals(
                List.of(rockets.get(1), rockets.get(2)),
                repository.findByTypeRegex(buildSearchRegex(condition))
        );
    }

    @Test
    void findByMissionNameRegex() {
        SearchCondition condition = new SearchCondition("missionName",
                SearchCondition.Condition.EQUALS,
                "Jupiter L2 communication probe deploy");

        assertIterableEquals(
                List.of(rockets.get(2)),
                repository.findByMissionNameRegex(buildSearchRegex(condition))
        );
    }

    @Test
    void findByTypeAndMissionNameRegexes() {
        SearchCondition missionNameCondition = new SearchCondition("missionName",
                SearchCondition.Condition.LIKE,
                "deploy");
        SearchCondition typeCondition = new SearchCondition("type",
                SearchCondition.Condition.NOT_EQUALS,
                "heavy");

        assertIterableEquals(
                List.of(rockets.get(2)),
                repository.findByTypeAndMissionNameRegexes(buildSearchRegex(typeCondition),
                        buildSearchRegex(missionNameCondition))
        );
    }

}