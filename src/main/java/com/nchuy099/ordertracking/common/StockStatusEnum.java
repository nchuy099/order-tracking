package com.nchuy099.ordertracking.common;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Locale;

public enum StockStatusEnum {
    IN_STOCK("IN_STOCK"),
    LIMITED_STOCK("LIMITED_STOCK"),
    OUT_OF_STOCK("out_of_stock");

    private final String value;

    StockStatusEnum(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static StockStatusEnum fromValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalizedValue = value.trim();
        for (StockStatusEnum stockStatus : values()) {
            if (stockStatus.name().equalsIgnoreCase(normalizedValue)
                    || stockStatus.value.equalsIgnoreCase(normalizedValue)) {
                return stockStatus;
            }
        }

        try {
            return StockStatusEnum.valueOf(normalizedValue.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
