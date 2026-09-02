package com.nchuy099.ordertracking.common;

public enum OrderStatusEnum {
    AWAITING_PAYMENT,
    PENDING,
    CONFIRMED,
    PICKING,
    SHIPPING,
    DELIVERED,
    FAILED,
    REATTEMPT,
    RETURNING,
    RETURNED,
    CANCELLED
}
