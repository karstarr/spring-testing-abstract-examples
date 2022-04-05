package com.griddynamics.akarsakov.services.search;

import org.springframework.lang.NonNull;

public record SearchCondition(String attributeName, Condition condition, Object value) {
    public enum Condition {
        NOT_EQUALS(false),
        EQUALS(false),
        LESSER_THAN(true),
        GREATER_THAN(true),
        LESSER_OR_EQUALS_THAN(true),
        GREATER_OR_EQUALS_THAN(true),
        LIKE(false);

        private final boolean numericOnly;

        Condition (boolean numericOnly) {
            this.numericOnly = numericOnly;
        }

        public boolean isNumericOnly() {
            return numericOnly;
        }
    }

    public boolean isAttributeNameSimilar(@NonNull String name) {
        return name.equalsIgnoreCase(attributeName);
    }
}
