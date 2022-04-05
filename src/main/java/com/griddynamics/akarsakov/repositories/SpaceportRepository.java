package com.griddynamics.akarsakov.repositories;

import com.griddynamics.akarsakov.entities.Spaceport;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.UUID;

public interface SpaceportRepository extends MongoRepository<Spaceport, UUID> {

    List<Spaceport> findByNameIgnoreCase(String name);

    List<Spaceport> findByNameIgnoreCase(String name, Pageable pageable);

    List<Spaceport> findByNameLikeIgnoreCase(String namePart);

    List<Spaceport> findByNameLikeIgnoreCase(String namePart, Pageable pageable);

}
