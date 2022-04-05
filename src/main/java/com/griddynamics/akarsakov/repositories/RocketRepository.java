package com.griddynamics.akarsakov.repositories;

import com.griddynamics.akarsakov.entities.Rocket;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.util.Pair;

import java.util.List;
import java.util.UUID;

public interface RocketRepository extends MongoRepository<Rocket, UUID> {

    @Query("{'id' : { '$nin' : ?0 }}")
    List<Rocket> findByIdNotIn(List<UUID> ids);

    @Query("{'type' : { '$regex' : ?#{[0].first} , '$options' : ?#{[0].second} }}")
    List<Rocket> findByTypeRegex(Pair<String, String> searchParams);

    @Query("{'missionName' : { '$regex' : ?#{[0].first} , '$options' : ?#{[0].second} }}")
    List<Rocket> findByMissionNameRegex(Pair<String, String> missionNameRegex);

    @Query("{'type' : { '$regex' : ?#{[0].first} , '$options' : ?#{[0].second} }, " +
            "'missionName' : { '$regex' : ?#{[1].first} , '$options' : ?#{[1].second} }}")
    List<Rocket> findByTypeAndMissionNameRegexes(Pair<String, String> typeRegex, Pair<String, String> missionNameRegex);

}
