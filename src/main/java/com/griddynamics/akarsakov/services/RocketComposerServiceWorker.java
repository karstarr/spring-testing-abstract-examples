package com.griddynamics.akarsakov.services;

import com.griddynamics.akarsakov.entities.Rocket;
import com.griddynamics.akarsakov.entities.Satellite;
import com.griddynamics.akarsakov.repositories.RocketRepository;
import com.griddynamics.akarsakov.services.search.SearchCondition;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Predicate;

import static com.griddynamics.akarsakov.utils.TextSearchConditionsTranslator.buildSearchRegex;

final class RocketComposerServiceWorker {
    private final RocketRepository rocketRepository;

    RocketComposerServiceWorker(RocketRepository rocketRepository) {
        this.rocketRepository = rocketRepository;
    }

    Rocket searchByConditions(List<SearchCondition> conditions) {
        Rocket rocket;

        Predicate<SearchCondition> hasId = (condition) -> condition.isAttributeNameSimilar("id");
        SearchCondition rocketIdCondition = extractRocketIdCondition(conditions, hasId);
        UUID rocketId = (UUID) rocketIdCondition.value();

        List<Rocket> rockets;

        if (rocketId != null) {
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

            rocket = filterRockets(rockets, conditions.stream().filter(hasId.negate()).toList());
        } else {
            Predicate<SearchCondition> hasType = (condition) -> condition.isAttributeNameSimilar("type");
            Predicate<SearchCondition> hasMissionName = (condition) -> condition.isAttributeNameSimilar("missionName");

            rockets = findRockets(conditions, hasType, hasMissionName);
            rocket = filterRockets(rockets,
                    sanitizeConditions(conditions, List.of(hasId, hasType, hasMissionName)));
        }

        return rocket;
    }

    SearchCondition extractRocketIdCondition(List<SearchCondition> conditions, Predicate<SearchCondition> hasId) {
        if (conditions == null || hasId == null) {
            return new SearchCondition(null, null, null);
        }
        return conditions.stream()
                .filter(hasId)
                .findFirst()
                .orElse(new SearchCondition(null, null, null));
    }

    Rocket filterRocket(Rocket rocket, List<SearchCondition> conditions) {
        if (rocket == null) {
            return null;
        }
        if (conditions == null) {
            return rocket;
        }

        boolean everythingMatches = conditions.stream().allMatch(condition -> checkCondition(rocket, condition));
        return everythingMatches ? rocket : null;
    }

    List<Rocket> findRockets(List<SearchCondition> conditions,
                             Predicate<SearchCondition> hasType,
                             Predicate<SearchCondition> hasMissionName) {
        List<Rocket> candidateRockets = new LinkedList<>();

        if (conditions == null) {
            return candidateRockets;
        }

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

    List<SearchCondition> sanitizeConditions(List<SearchCondition> conditions,
                                             List<Predicate<SearchCondition>> excludePredicates) {
        if (conditions == null) {
            return new ArrayList<>();
        }
        if (excludePredicates == null) {
            return conditions;
        }
        return conditions.stream()
                .filter(condition -> excludePredicates.stream()
                        .noneMatch(predicate -> predicate.test(condition)))
                .toList();
    }

    Rocket filterRockets(List<Rocket> rockets, List<SearchCondition> conditions) {
        if (rockets != null) {
            return rockets.stream()
                    .filter(rocket -> filterRocket(rocket, conditions) != null)
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    boolean checkCondition(Rocket rocket, SearchCondition condition) {
        if (rocket == null) {
            return false;
        }
        if (condition == null) {
            return true;
        }

        String parameterValue;
        if (condition.isAttributeNameSimilar("type")) {
            parameterValue = rocket.getType();
        } else if (condition.isAttributeNameSimilar("missionName")) {
            parameterValue = rocket.getMissionName();
        } else if (condition.isAttributeNameSimilar("spaceport")) {
            parameterValue = rocket.getSpaceport().toString();
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

    boolean checkParameter(String paramValue, SearchCondition condition) {
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
            case LESSER_THAN -> isSatisfied = new BigDecimal(paramValue).compareTo(new BigDecimal(value)) < 0;
            case GREATER_THAN -> isSatisfied = new BigDecimal(paramValue).compareTo(new BigDecimal(value)) > 0;
            case LESSER_OR_EQUALS_THAN ->
                    isSatisfied = new BigDecimal(paramValue).compareTo(new BigDecimal(value)) <= 0;
            case GREATER_OR_EQUALS_THAN ->
                    isSatisfied = new BigDecimal(paramValue).compareTo(new BigDecimal(value)) >= 0;
        }
        return isSatisfied;
    }

    boolean checkSatellite(Rocket rocket, SearchCondition condition) {
        Predicate<String> satelliteParameterCondition = (parameter) -> {
            boolean isParameterSatisfying;
            switch (condition.condition()) {
                case NOT_EQUALS -> isParameterSatisfying = !Objects.equals(condition.value(), parameter);
                case EQUALS -> isParameterSatisfying = Objects.equals(condition.value(), parameter);
                case LIKE -> isParameterSatisfying = parameter.contains(condition.value().toString());
                default -> isParameterSatisfying = false;
            }
            return isParameterSatisfying;
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

    static boolean isNumeric(String valueToCheck) {
        if (valueToCheck == null) {
            return true;
        }
        try {
            Double.parseDouble(valueToCheck.toLowerCase());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
