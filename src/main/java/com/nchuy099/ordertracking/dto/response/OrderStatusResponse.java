package com.nchuy099.ordertracking.dto.response;

import com.nchuy099.ordertracking.common.OrderStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusResponse {

    private UUID orderId;
    private String orderCode;
    private OrderStatusEnum status;
    private LocalDateTime cancelledAt;
}
