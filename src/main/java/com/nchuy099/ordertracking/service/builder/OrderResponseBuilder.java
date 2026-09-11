package com.nchuy099.ordertracking.service.builder;

import com.nchuy099.ordertracking.dto.response.DailyOrderSummaryResponse;
import com.nchuy099.ordertracking.dto.response.OrderDetailResponse;
import com.nchuy099.ordertracking.dto.response.OrderListResponse;
import com.nchuy099.ordertracking.dto.response.OrderStatusResponse;
import com.nchuy099.ordertracking.dto.response.OrderSummaryResponse;
import com.nchuy099.ordertracking.dto.response.PlaceOrderResponse;
import com.nchuy099.ordertracking.entity.OrderEntity;
import com.nchuy099.ordertracking.entity.OrderItemEntity;
import com.nchuy099.ordertracking.entity.PaymentEntity;
import com.nchuy099.ordertracking.repository.DailyOrderSummaryProjection;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.List;

public final class OrderResponseBuilder {

    private OrderResponseBuilder() {
    }

    public static OrderSummaryResponse buildOrderSummary(
            BigDecimal subTotal,
            BigDecimal discountAmount,
            BigDecimal shippingFee
    ) {
        return OrderSummaryResponse.builder()
                .subTotal(subTotal)
                .discountAmount(discountAmount)
                .shippingFee(shippingFee)
                .grandTotal(subTotal.subtract(discountAmount).add(shippingFee))
                .build();
    }

    public static PlaceOrderResponse buildPlaceOrder(OrderEntity order, PaymentEntity payment, BigDecimal grandTotal) {
        return PlaceOrderResponse.builder()
                .orderId(order.getId().toString())
                .orderCode(order.getCode())
                .paymentId(payment.getId().toString())
                .paymentCode(payment.getPaymentCode())
                .grandTotal(grandTotal)
                .build();
    }

    public static OrderDetailResponse buildOrderDetail(
            OrderEntity order,
            List<OrderDetailResponse.OrderItemResponse> items,
            OrderDetailResponse.PaymentResponse payment
    ) {
        return OrderDetailResponse.builder()
                .orderId(order.getId())
                .orderCode(order.getCode())
                .status(order.getStatus())
                .orderedAt(order.getOrderedAt())
                .cancelledAt(order.getCancelledAt())
                .completedAt(order.getCompletedAt())
                .note(order.getNote())
                .items(items)
                .pricing(buildPricing(order))
                .shipping(buildShipping(order))
                .payment(payment)
                .build();
    }

    public static DailyOrderSummaryResponse buildDailyOrderSummary(DailyOrderSummaryProjection summary) {
        return DailyOrderSummaryResponse.builder()
                .totalOrdersToday(summary.getTotalOrdersToday())
                .deliveredOrdersToday(summary.getDeliveredOrdersToday())
                .pendingOrdersToday(summary.getPendingOrdersToday())
                .build();
    }

    public static OrderStatusResponse buildOrderStatus(OrderEntity order) {
        return OrderStatusResponse.builder()
                .orderId(order.getId())
                .orderCode(order.getCode())
                .status(order.getStatus())
                .cancelledAt(order.getCancelledAt())
                .build();
    }

    public static OrderListResponse buildOrderList(
            Page<OrderEntity> orders,
            List<OrderListResponse.OrderResponse> content
    ) {
        return OrderListResponse.builder()
                .content(content)
                .page(orders.getNumber())
                .size(orders.getSize())
                .totalElements(orders.getTotalElements())
                .totalPages(orders.getTotalPages())
                .build();
    }

    public static OrderListResponse.OrderResponse buildOrderListItem(
            OrderEntity order,
            List<OrderDetailResponse.OrderItemResponse> items,
            OrderDetailResponse.PaymentResponse payment,
            boolean includeCustomerName
    ) {
        return OrderListResponse.OrderResponse.builder()
                .orderId(order.getId())
                .orderCode(order.getCode())
                .status(order.getStatus())
                .orderedAt(order.getOrderedAt())
                .cancelledAt(order.getCancelledAt())
                .completedAt(order.getCompletedAt())
                .note(order.getNote())
                .customerName(includeCustomerName ? order.getUser().getFullName() : null)
                .items(items)
                .pricing(buildPricing(order))
                .shipping(buildShipping(order))
                .payment(payment)
                .build();
    }

    public static OrderDetailResponse.OrderItemResponse buildOrderItem(OrderItemEntity orderItem) {
        return OrderDetailResponse.OrderItemResponse.builder()
                .orderItemId(orderItem.getId())
                .productVariantId(orderItem.getProductVariant().getId())
                .productName(orderItem.getProductName())
                .productPrimaryImageUrl(orderItem.getProductVariant().getProduct().getPrimaryImageUrl())
                .variantName(orderItem.getVariantName())
                .sku(orderItem.getSku())
                .unitPrice(orderItem.getUnitPrice())
                .quantity(orderItem.getQuantity())
                .lineTotal(orderItem.getUnitPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity())))
                .build();
    }

    public static OrderDetailResponse.PaymentResponse buildPayment(PaymentEntity payment) {
        return OrderDetailResponse.PaymentResponse.builder()
                .paymentId(payment.getId())
                .paymentCode(payment.getPaymentCode())
                .method(payment.getMethod())
                .status(payment.getStatus())
                .amount(payment.getAmount())
                .paidAt(payment.getPaidAt())
                .build();
    }

    private static OrderDetailResponse.PricingResponse buildPricing(OrderEntity order) {
        return OrderDetailResponse.PricingResponse.builder()
                .subTotal(order.getSubTotal())
                .discountAmount(order.getDiscountAmount())
                .shippingFee(order.getShippingFee())
                .grandTotal(order.getGrandTotal())
                .build();
    }

    private static OrderDetailResponse.ShippingResponse buildShipping(OrderEntity order) {
        return OrderDetailResponse.ShippingResponse.builder()
                .recipientName(order.getRecipientName())
                .recipientPhone(order.getRecipientPhone())
                .shippingAddress(order.getShippingAddress())
                .build();
    }
}
