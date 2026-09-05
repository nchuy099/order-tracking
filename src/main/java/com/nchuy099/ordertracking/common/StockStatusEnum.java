package com.nchuy099.ordertracking.common;

import com.fasterxml.jackson.annotation.JsonValue;

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
}
