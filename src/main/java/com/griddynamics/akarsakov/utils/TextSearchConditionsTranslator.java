package com.griddynamics.akarsakov.utils;

import com.griddynamics.akarsakov.services.search.SearchCondition;
import org.springframework.data.util.Pair;

public final class TextSearchConditionsTranslator {

    private TextSearchConditionsTranslator() {}

    public static Pair<String, String> buildSearchRegex(SearchCondition condition) {
        StringBuilder searchRegex = new StringBuilder();
        String options = "";

        switch (condition.condition()) {
            case NOT_EQUALS -> {
                searchRegex.append("^(?!").append(condition.value()).append("$)");
                options = "s";
            }
            case EQUALS -> searchRegex.append("^").append(condition.value()).append("$");
            case LIKE -> {
                searchRegex.append(".*").append(condition.value()).append(".*");
                options = "i";
            }
        }

        return Pair.of(searchRegex.toString(), options);
    }

}
