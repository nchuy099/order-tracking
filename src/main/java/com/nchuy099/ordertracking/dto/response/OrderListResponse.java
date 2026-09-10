package com.nchuy099.ordertracking.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.nchuy099.ordertracking.common.OrderStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderListResponse {

    private List<OrderResponse> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderResponse {
        private UUID orderId;
        private String orderCode;
        private OrderStatusEnum status;
        private LocalDateTime orderedAt;
        private LocalDateTime cancelledAt;
        private LocalDateTime completedAt;
        private String note;
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private String customerName;
        private List<OrderDetailResponse.OrderItemResponse> items;
        private OrderDetailResponse.PricingResponse pricing;
        private OrderDetailResponse.ShippingResponse shipping;
        private OrderDetailResponse.PaymentResponse payment;
    }
}
