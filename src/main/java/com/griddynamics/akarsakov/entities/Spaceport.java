package com.griddynamics.akarsakov.entities;

import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;
import org.springframework.lang.NonNull;

import java.util.Objects;
import java.util.UUID;

@Document
public class Spaceport {
    @MongoId
    private final UUID id;

    @Field
    private String name;

    @Field
    private final Double longitude;

    @Field
    private final Double latitude;

    @PersistenceConstructor
    public Spaceport(@NonNull UUID id, @NonNull Double longitude, @NonNull Double latitude) {
        this.id = id;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getLongitude() {
        return longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    @Override
    public String toString() {
        return "{\"id\" : \"" + id + "\"," +
                "\"name\" : \"" + name + "\"," +
                "\"longitude\" : " + longitude + "," +
                "\"latitude\" : " + latitude + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o == this) {
            return true;
        }
        if (o instanceof Spaceport) {
            return Objects.equals(((Spaceport) o).getId(), this.id)
                    && Objects.equals(((Spaceport) o).getLatitude(), this.latitude)
                    && Objects.equals(((Spaceport) o).getLongitude(), this.longitude);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, latitude, longitude);
    }
}
