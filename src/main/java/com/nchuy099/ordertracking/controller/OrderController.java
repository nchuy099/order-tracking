package com.nchuy099.ordertracking.controller;

import com.nchuy099.ordertracking.dto.request.OrderSummaryRequest;
import com.nchuy099.ordertracking.dto.request.PlaceOrderRequest;
import com.nchuy099.ordertracking.dto.response.OrderSummaryResponse;
import com.nchuy099.ordertracking.dto.response.PlaceOrderResponse;
import com.nchuy099.ordertracking.dto.response.OrderDetailResponse;
import com.nchuy099.ordertracking.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/{orderId}/details")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<OrderDetailResponse> getDetails(@PathVariable UUID orderId) {
        OrderDetailResponse response = orderService.getDetails(orderId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/summary")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<OrderSummaryResponse> getSummary(@RequestBody OrderSummaryRequest request) {
        OrderSummaryResponse response = orderService.getSummary(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<PlaceOrderResponse> placeOrder(@RequestBody PlaceOrderRequest request) {
        PlaceOrderResponse response = orderService.placeOrder(request);
        return ResponseEntity.ok(response);
    }
}
