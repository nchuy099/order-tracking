package com.nchuy099.ordertracking.controller;

import com.nchuy099.ordertracking.dto.request.OrderSummaryRequest;
import com.nchuy099.ordertracking.dto.request.PlaceOrderRequest;
import com.nchuy099.ordertracking.dto.response.OrderSummaryResponse;
import com.nchuy099.ordertracking.dto.response.PlaceOrderResponse;
import com.nchuy099.ordertracking.dto.response.OrderDetailResponse;
import com.nchuy099.ordertracking.dto.response.OrderStatusResponse;
import com.nchuy099.ordertracking.dto.response.OrderListResponse;
import com.nchuy099.ordertracking.dto.response.DailyOrderSummaryResponse;
import com.nchuy099.ordertracking.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderListResponse> getOrders(
            @RequestParam(required = false) List<String> status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        OrderListResponse response = orderService.getOrders(status, page, size, sortBy, sortDir);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<OrderListResponse> getMyOrders(
            @RequestParam(required = false) List<String> status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        OrderListResponse response = orderService.getMyOrders(status, page, size, sortBy, sortDir);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/daily-summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DailyOrderSummaryResponse> getDailySummary() {
        DailyOrderSummaryResponse response = orderService.getDailySummary();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{orderId}/details")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<OrderDetailResponse> getDetails(@PathVariable UUID orderId) {
        OrderDetailResponse response = orderService.getDetails(orderId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{orderId}/confirm")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderStatusResponse> confirmOrder(@PathVariable UUID orderId) {
        OrderStatusResponse response = orderService.confirmOrder(orderId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{orderId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderStatusResponse> rejectOrder(@PathVariable UUID orderId) {
        OrderStatusResponse response = orderService.rejectOrder(orderId);
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
