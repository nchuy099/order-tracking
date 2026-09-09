package com.nchuy099.ordertracking.controller;

import com.nchuy099.ordertracking.dto.request.CartRequest;
import com.nchuy099.ordertracking.dto.request.CreateCategoryRequest;
import com.nchuy099.ordertracking.dto.response.CartResponse;
import com.nchuy099.ordertracking.entity.CartEntity;
import com.nchuy099.ordertracking.entity.CategoryEntity;
import com.nchuy099.ordertracking.service.CartService;
import com.nchuy099.ordertracking.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CartResponse> get() {
        CartResponse cartResponse = cartService.get();
        return ResponseEntity.ok(cartResponse);
    }


    @PostMapping("/items")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Void> addItem(@RequestBody CartRequest request) {
    cartService.addItem(request);
    return ResponseEntity.ok(null);
    }

    @PutMapping("/items")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Void> updateItemQuantity(@RequestBody CartRequest request) {
        cartService.updateItemQuantity(request);
        return ResponseEntity.ok(null);
    }
}
