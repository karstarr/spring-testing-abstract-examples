package com.griddynamics.akarsakov.utils;

import com.griddynamics.akarsakov.services.search.SearchCondition;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.data.util.Pair;

import static com.griddynamics.akarsakov.services.search.SearchCondition.Condition.*;
import static com.griddynamics.akarsakov.services.search.SearchCondition.Condition.GREATER_THAN;
import static org.junit.jupiter.api.Assertions.*;

@Tag("unitTest")
class TextSearchConditionsTranslatorTest {

    @Test
    void buildSearchRegex_NOT_EQUALS_case() {
        SearchCondition condition = new SearchCondition("randParamName", NOT_EQUALS ,412);
        Pair<String, String> expected = Pair.of("^(?!412$)", "s");

        assertEquals(expected, TextSearchConditionsTranslator.buildSearchRegex(condition));
    }

    @Test
    void buildSearchRegex_EQUALS_case() {
        SearchCondition condition = new SearchCondition("randParamName", EQUALS ,412);
        Pair<String, String> expected = Pair.of("^412$", "");

        assertEquals(expected, TextSearchConditionsTranslator.buildSearchRegex(condition));
    }

    @Test
    void buildSearchRegex_LIKE_case() {
        SearchCondition condition = new SearchCondition("randParamName", LIKE ,412);
        Pair<String, String> expected = Pair.of(".*412.*", "i");

        assertEquals(expected, TextSearchConditionsTranslator.buildSearchRegex(condition));
    }

    @Test
    void buildSearchRegex_default_case() {
        SearchCondition condition = new SearchCondition("randParamName", GREATER_THAN ,412);
        Pair<String, String> expected = Pair.of("", "");

        assertEquals(expected, TextSearchConditionsTranslator.buildSearchRegex(condition));
    }

}