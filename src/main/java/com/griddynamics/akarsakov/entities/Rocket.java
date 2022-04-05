package com.griddynamics.akarsakov.entities;

import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;
import org.springframework.lang.NonNull;

import java.util.*;

@Document
public class Rocket {
    @MongoId
    private final UUID id;

    @Field
    private final String type;

    @Field
    private String missionName;

    @DBRef
    private Spaceport spaceport;

    private Map<String, String> parameters = new HashMap<>();

    private Set<Satellite> satellites = new HashSet<>();

    @PersistenceConstructor
    public Rocket(@NonNull UUID id, @NonNull String type) {
        this.id = id;
        this.type = type;
    }


    public UUID getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getMissionName() {
        return missionName;
    }

    public void setMissionName(String missionName) {
        this.missionName = missionName;
    }

    public Spaceport getSpaceport() {
        return spaceport;
    }

    public void setSpaceport(Spaceport spaceport) {
        this.spaceport = spaceport;
    }

    public Map<String, String> getParameters() {
        return Collections.unmodifiableMap(parameters);
    }

    public void addParameter(@NonNull String paramName, @NonNull String paramValue) {
        parameters.put(paramName, paramValue);
    }

    public void setParameters(Map<String, String> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            this.parameters.clear();
        } else {
            parameters.forEach((key, value) -> {
                if (key != null) {
                    if (value != null) {
                        this.parameters.put(key, value);
                    } else {
                        this.parameters.remove(key);
                    }
                }
            });
        }
    }

    public void removeParameter(@NonNull String paramName) {
        parameters.remove(paramName);
    }

    public Set<Satellite> getSatellites() {
        return Collections.unmodifiableSet(satellites);
    }

    public void addSatellite(@NonNull Satellite satellite) {
        satellites.add(satellite);
    }

    public void removeSatellite(@NonNull Satellite satellite) {
        satellites.remove(satellite);
    }

    public void setSatellites(Set<Satellite> satellites) {
        this.satellites.clear();
        if (satellites != null && !satellites.isEmpty()) {
            satellites.stream()
                    .filter(Objects::nonNull)
                    .forEach(this.satellites::add);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o == this) {
            return true;
        }
        if (o instanceof Rocket) {
            return Objects.equals(((Rocket) o).getId(), this.id)
                    && Objects.equals(((Rocket) o).getType(), this.type);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type);
    }
}
