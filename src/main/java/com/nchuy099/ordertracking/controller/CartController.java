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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartResponse> get() {
        CartResponse cartResponse = cartService.get();
        return ResponseEntity.ok(cartResponse);
    }


    @PostMapping("/items")
    public ResponseEntity<Void> addItem(@RequestBody CartRequest request) {
    cartService.addItem(request);
    return ResponseEntity.ok(null);
    }

    @PutMapping("/items")
    public ResponseEntity<Void> updateItemQuantity(@RequestBody CartRequest request) {
        cartService.updateItemQuantity(request);
        return ResponseEntity.ok(null);
    }
}
