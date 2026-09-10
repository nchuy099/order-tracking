package com.nchuy099.ordertracking.service;

import com.nchuy099.ordertracking.dto.request.OrderSummaryRequest;
import com.nchuy099.ordertracking.dto.request.PlaceOrderRequest;
import com.nchuy099.ordertracking.dto.response.OrderSummaryResponse;
import com.nchuy099.ordertracking.dto.response.PlaceOrderResponse;
import com.nchuy099.ordertracking.dto.response.OrderDetailResponse;
import com.nchuy099.ordertracking.dto.response.OrderStatusResponse;
import com.nchuy099.ordertracking.dto.response.OrderListResponse;
import com.nchuy099.ordertracking.dto.response.DailyOrderSummaryResponse;

import java.util.List;
import java.util.UUID;

public interface OrderService {

    OrderSummaryResponse getSummary(OrderSummaryRequest request);

    PlaceOrderResponse placeOrder(PlaceOrderRequest request);

    OrderDetailResponse getDetails(UUID orderId);

    OrderListResponse getOrders(List<String> status, int page, int size, String sortBy, String sortDir);

    OrderListResponse getMyOrders(List<String> status, int page, int size, String sortBy, String sortDir);

    DailyOrderSummaryResponse getDailySummary();

    OrderStatusResponse confirmOrder(UUID orderId);

    OrderStatusResponse rejectOrder(UUID orderId);
}
