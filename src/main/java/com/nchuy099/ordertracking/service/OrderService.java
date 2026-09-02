package com.nchuy099.ordertracking.service;

import com.nchuy099.ordertracking.dto.request.OrderSummaryRequest;
import com.nchuy099.ordertracking.dto.request.PlaceOrderRequest;
import com.nchuy099.ordertracking.dto.response.OrderSummaryResponse;
import com.nchuy099.ordertracking.dto.response.PlaceOrderResponse;

public interface OrderService {

    OrderSummaryResponse getSummary(OrderSummaryRequest request);

    PlaceOrderResponse placeOrder(PlaceOrderRequest request);
}
