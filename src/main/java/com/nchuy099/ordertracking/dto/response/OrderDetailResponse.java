package com.nchuy099.ordertracking.dto.response;

import com.nchuy099.ordertracking.common.OrderStatusEnum;
import com.nchuy099.ordertracking.common.PaymentMethodEnum;
import com.nchuy099.ordertracking.common.PaymentStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailResponse {

    private UUID orderId;
    private String orderCode;
    private OrderStatusEnum status;
    private LocalDateTime orderedAt;
    private LocalDateTime cancelledAt;
    private LocalDateTime completedAt;
    private String note;
    private List<OrderItemResponse> items;
    private PricingResponse pricing;
    private ShippingResponse shipping;
    private PaymentResponse payment;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemResponse {
        private UUID orderItemId;
        private UUID productVariantId;
        private String productName;
        private String productPrimaryImageUrl;
        private String variantName;
        private String sku;
        private BigDecimal unitPrice;
        private Integer quantity;
        private BigDecimal lineTotal;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PricingResponse {
        private BigDecimal subTotal;
        private BigDecimal discountAmount;
        private BigDecimal shippingFee;
        private BigDecimal grandTotal;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShippingResponse {
        private String recipientName;
        private String recipientPhone;
        private String shippingAddress;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentResponse {
        private UUID paymentId;
        private String paymentCode;
        private PaymentMethodEnum method;
        private PaymentStatusEnum status;
        private BigDecimal amount;
        private LocalDateTime paidAt;
    }
}
