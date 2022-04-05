package com.griddynamics.akarsakov.repositories;

import com.griddynamics.akarsakov.TestingSpringBootApplication;
import com.griddynamics.akarsakov.entities.Spaceport;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
@DataMongoTest
@ExtendWith(SpringExtension.class)
@DirtiesContext
@ContextConfiguration(classes = {TestingSpringBootApplication.class})
class SpaceportRepositoryIntegrationTest {
    @Autowired
    private SpaceportRepository repository;

    private static List<Spaceport> spaceports;

    @BeforeAll
    static void initDatabase(@Autowired MongoTemplate mongoTemplate) {
        spaceports = List.of(
                new Spaceport(UUID.randomUUID(), 25.0, 40.5),
                new Spaceport(UUID.randomUUID(), 28.0, 43.5),
                new Spaceport(UUID.randomUUID(), 24.17, 41.0)
        );

        spaceports.get(0).setName("Main launch table");
        spaceports.get(1).setName("Main planetary port");
        spaceports.get(2).setName("Main planetary port");


        spaceports.forEach(mongoTemplate::save);
    }

    @AfterAll
    static void cleanup(@Autowired MongoTemplate mongoTemplate) {
        spaceports.forEach(mongoTemplate::remove);
    }

    @Test
    void findByNameIgnoreCase() {
        List<Spaceport> foundDocs = repository.findByNameIgnoreCase("main laUnch tabLe");

        assertEquals(1, foundDocs.size());
        assertEquals("Main launch table", foundDocs.get(0).getName());
        assertEquals(40.5, foundDocs.get(0).getLatitude());
    }

    @Test
    void findByNameIgnoreCase_pageable() {
        List<Spaceport> foundDocs = repository
                .findByNameIgnoreCase("Main planetary port", Pageable.ofSize(1).withPage(1));

        assertEquals(1, foundDocs.size());
        assertEquals(24.17, foundDocs.get(0).getLongitude());
    }

    @Test
    void findByNameLikeIgnoreCase() {
        List<Spaceport> foundDocs = repository.findByNameLikeIgnoreCase("PlaNetaRy");

        assertEquals(2, foundDocs.size());
        assertEquals(28.0, foundDocs.get(0).getLongitude());
        assertEquals(41.0, foundDocs.get(1).getLatitude());
    }

    @Test
    void findByNameLikeIgnoreCase_pageable() {
        List<Spaceport> foundDocs = repository.findByNameLikeIgnoreCase("netaRy", Pageable.ofSize(1).withPage(1));

        assertEquals(1, foundDocs.size());
        assertEquals(24.17, foundDocs.get(0).getLongitude());
    }
}