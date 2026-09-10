package com.nchuy099.ordertracking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyOrderSummaryResponse {

    private Long totalOrdersToday;
    private Long deliveredOrdersToday;
    private Long pendingOrdersToday;
}
