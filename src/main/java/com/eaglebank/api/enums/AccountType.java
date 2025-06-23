package com.eaglebank.api.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum AccountType {
    PERSONAL("personal"),
    BUSINESS("business"),
    SAVINGS("savings");

    private final String value;

    AccountType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static AccountType fromValue(String value) {
        for (AccountType type : AccountType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid account type: " + value);
    }
}
