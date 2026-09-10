package com.nchuy099.ordertracking.repository;

public interface DailyOrderSummaryProjection {

    Long getTotalOrdersToday();

    Long getDeliveredOrdersToday();

    Long getPendingOrdersToday();
}
