package com.griddynamics.akarsakov.services;

import com.griddynamics.akarsakov.entities.Rocket;
import com.griddynamics.akarsakov.entities.Satellite;
import com.griddynamics.akarsakov.repositories.RocketRepository;
import com.griddynamics.akarsakov.services.search.SearchCondition;

import java.util.*;
import java.util.function.Predicate;

import static com.griddynamics.akarsakov.utils.TextSearchConditionsTranslator.buildSearchRegex;
import static com.griddynamics.akarsakov.utils.NumberUtils.isNumeric;
import static com.griddynamics.akarsakov.utils.NumberUtils.compareParamAndConditionValues;

public final class RocketComposerServiceWorker {
    private final RocketRepository rocketRepository;

    public RocketComposerServiceWorker(RocketRepository rocketRepository) {
        this.rocketRepository = rocketRepository;
    }

    public Rocket searchByConditions(List<SearchCondition> conditions) {
        List<SearchCondition> nonNullConditions = conditions != null ?
                conditions.stream().filter(Objects::nonNull).toList() :
                null;

        Predicate<SearchCondition> hasId = (condition) -> condition.isAttributeNameSimilar("id");
        SearchCondition rocketIdCondition = extractRocketIdCondition(nonNullConditions, hasId);
        UUID rocketId = (UUID) rocketIdCondition.value();

        return rocketId != null ?
                findRocketWithIdInConditions(rocketId, rocketIdCondition, nonNullConditions.stream().filter(hasId.negate()).toList()) :
                findRocketWithoutIdInConditions(nonNullConditions, hasId);
    }

    private SearchCondition extractRocketIdCondition(List<SearchCondition> conditions, Predicate<SearchCondition> hasId) {
        if (conditions == null) {
            return new SearchCondition(null, null, null);
        }
        return conditions.stream()
                .filter(hasId)
                .findFirst()
                .orElse(new SearchCondition(null, null, null));
    }

    private Rocket findRocketWithIdInConditions(UUID rocketId,
                                                SearchCondition rocketIdCondition,
                                                List<SearchCondition> otherConditions) {
        List<Rocket> rockets;

        switch (rocketIdCondition.condition()) {
            case NOT_EQUALS -> rockets = rocketRepository.findByIdNotIn(List.of(rocketId));
            case EQUALS -> {
                rockets = new ArrayList<>();
                rockets.add(rocketRepository.findById(rocketId).orElse(null));
            }
            default -> throw new IllegalArgumentException("Search condition "
                    + rocketIdCondition.condition().name()
                    + " is not supported for the rocket ID parameter");
        }

        return filterRockets(rockets, otherConditions);
    }

    private Rocket findRocketWithoutIdInConditions(List<SearchCondition> conditions, Predicate<SearchCondition> hasId) {
        List<Rocket> rockets;

        Predicate<SearchCondition> hasType = (condition) -> condition.isAttributeNameSimilar("type");
        Predicate<SearchCondition> hasMissionName = (condition) -> condition.isAttributeNameSimilar("missionName");

        rockets = findRockets(conditions, hasType, hasMissionName);
        return filterRockets(rockets, sanitizeConditions(conditions, List.of(hasId, hasType, hasMissionName)));
    }

    private List<Rocket> findRockets(List<SearchCondition> conditions,
                                     Predicate<SearchCondition> hasType,
                                     Predicate<SearchCondition> hasMissionName) {
        List<Rocket> candidateRockets = new LinkedList<>();

        if (conditions == null) {
            return candidateRockets;
        }

        SearchCondition[] mainSearchConditions = findMainRocketSearchConditions(conditions, hasType, hasMissionName);
        SearchCondition typeCondition = mainSearchConditions[0];
        SearchCondition missionNameCondition = mainSearchConditions[1];

        if (typeCondition == null && missionNameCondition == null) {
            candidateRockets = rocketRepository.findAll();
        } else {
            if (typeCondition != null && missionNameCondition != null){
                candidateRockets = rocketRepository.findByTypeAndMissionNameRegexes(buildSearchRegex(typeCondition),
                        buildSearchRegex(missionNameCondition));
            }
            if (typeCondition != null && missionNameCondition == null) {
                candidateRockets = rocketRepository.findByTypeRegex(buildSearchRegex(typeCondition));
            }
            if (typeCondition == null) {
                candidateRockets = rocketRepository.findByMissionNameRegex(buildSearchRegex(missionNameCondition));
            }
        }

        return candidateRockets;
    }

    private Rocket filterRockets(List<Rocket> rockets, List<SearchCondition> conditions) {
        return rockets.stream()
                .filter(rocket -> filterRocket(rocket, conditions) != null)
                .findFirst()
                .orElse(null);
    }

    private List<SearchCondition> sanitizeConditions(List<SearchCondition> conditions,
                                                     List<Predicate<SearchCondition>> excludePredicates) {
        if (conditions == null) {
            return new ArrayList<>();
        }
        return conditions.stream()
                .filter(condition -> excludePredicates.stream()
                        .noneMatch(predicate -> predicate.test(condition)))
                .toList();
    }

    private SearchCondition[] findMainRocketSearchConditions(List<SearchCondition> conditions,
                                                             Predicate<SearchCondition> hasType,
                                                             Predicate<SearchCondition> hasMissionName) {
        SearchCondition[] mainConditions = new SearchCondition[2];

        SearchCondition typeCondition = null;
        SearchCondition missionNameCondition = null;

        int foundConditions = 0;
        Iterator<SearchCondition> iterator = conditions.listIterator();
        while (foundConditions < 2 && iterator.hasNext()) {
            SearchCondition condition = iterator.next();
            if (hasType != null && hasType.test(condition)) {
                typeCondition = condition;
                foundConditions++;
            } else if (hasMissionName != null && hasMissionName.test(condition)) {
                missionNameCondition = condition;
                foundConditions++;
            }
        }

        mainConditions[0] = typeCondition;
        mainConditions[1] = missionNameCondition;

        return mainConditions;
    }

    private Rocket filterRocket(Rocket rocket, List<SearchCondition> conditions) {
        boolean everythingMatches = conditions.stream().allMatch(condition -> checkCondition(rocket, condition));
        return everythingMatches ? rocket : null;
    }

    private boolean checkCondition(Rocket rocket, SearchCondition condition) {
        if (rocket == null) {
            return false;
        }

        String parameterValue;
        if (condition.isAttributeNameSimilar("type")) {
            parameterValue = rocket.getType();
        } else if (condition.isAttributeNameSimilar("missionName")) {
            parameterValue = rocket.getMissionName();
        } else if (condition.isAttributeNameSimilar("spaceport")) {
            parameterValue = Objects.toString(rocket.getSpaceport(), null);
        } else {
            parameterValue = rocket.getParameters().get(condition.attributeName());
        }

        boolean isSatisfied = false;
        if (parameterValue != null) {
            isSatisfied = checkParameter(parameterValue, condition);
        } else if (!rocket.getSatellites().isEmpty()) {
            isSatisfied = checkSatellite(rocket, condition);
        }
        return isSatisfied;
    }

    private boolean checkParameter(String paramValue, SearchCondition condition) {
        boolean isSatisfied = false;
        String value = String.valueOf(condition.value());

        if (condition.condition().isNumericOnly() && !isNumeric(value)) {
            throw new IllegalArgumentException("Numerical search condition for "
                    + condition.attributeName() + " must have not null value.");
        }

        switch (condition.condition()) {
            case NOT_EQUALS -> isSatisfied = !Objects.equals(value, paramValue);
            case EQUALS -> isSatisfied = Objects.equals(value, paramValue);
            case LIKE -> isSatisfied = paramValue.contains(value);
            case LESSER_THAN -> isSatisfied = compareParamAndConditionValues(paramValue, value) < 0;
            case GREATER_THAN -> isSatisfied = compareParamAndConditionValues(paramValue, value) > 0;
            case LESSER_OR_EQUALS_THAN -> isSatisfied = compareParamAndConditionValues(paramValue, value) <= 0;
            case GREATER_OR_EQUALS_THAN -> isSatisfied = compareParamAndConditionValues(paramValue, value) >= 0;
        }
        return isSatisfied;
    }

    private boolean checkSatellite(Rocket rocket, SearchCondition condition) {
        Predicate<String> satelliteParameterCondition = (parameter) -> switch (condition.condition()) {
            case NOT_EQUALS -> !Objects.equals(condition.value(), parameter);
            case EQUALS -> Objects.equals(condition.value(), parameter);
            case LIKE -> Objects.toString(parameter, "").contains(condition.value().toString());
            default -> false;
        };

        boolean isSatisfied = false;

        if (condition.isAttributeNameSimilar("satellite.name")) {
            isSatisfied = rocket.getSatellites()
                    .stream()
                    .map(Satellite::getName)
                    .anyMatch(satelliteParameterCondition);
        }
        if (condition.isAttributeNameSimilar("satellite.assignment")) {
            isSatisfied = rocket.getSatellites()
                    .stream()
                    .map(Satellite::getAssignment)
                    .anyMatch(satelliteParameterCondition);
        }
        return isSatisfied;
    }
}
