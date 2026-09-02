package com.nchuy099.ordertracking.controller;

import com.nchuy099.ordertracking.dto.request.OrderSummaryRequest;
import com.nchuy099.ordertracking.dto.request.PlaceOrderRequest;
import com.nchuy099.ordertracking.dto.response.OrderSummaryResponse;
import com.nchuy099.ordertracking.dto.response.PlaceOrderResponse;
import com.nchuy099.ordertracking.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/summary")
    public ResponseEntity<OrderSummaryResponse> getSummary(@RequestBody OrderSummaryRequest request) {
        OrderSummaryResponse response = orderService.getSummary(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<PlaceOrderResponse> placeOrder(@RequestBody PlaceOrderRequest request) {
        PlaceOrderResponse response = orderService.placeOrder(request);
        return ResponseEntity.ok(response);
    }
}
