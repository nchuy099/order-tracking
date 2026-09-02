package com.nchuy099.ordertracking.controller;

import com.nchuy099.ordertracking.entity.DiscountEntity;
import com.nchuy099.ordertracking.service.DiscountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/discounts")
@RequiredArgsConstructor
public class DiscountController {

    private final DiscountService discountService;

    @GetMapping
    public ResponseEntity<List<DiscountEntity>> getAll() {
        List<DiscountEntity> discounts = discountService.getAll();
        return ResponseEntity.ok(discounts);
    }
}
