package com.nchuy099.ordertracking.controller;

import com.nchuy099.ordertracking.dto.request.CreateProductRequest;
import com.nchuy099.ordertracking.dto.request.CreateProductVariantRequest;
import com.nchuy099.ordertracking.entity.ProductEntity;
import com.nchuy099.ordertracking.entity.ProductVariantEntity;
import com.nchuy099.ordertracking.service.ProductService;
import com.nchuy099.ordertracking.service.ProductVariantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/product-variants")
@RequiredArgsConstructor
public class ProductVariantController {

    private final ProductVariantService productVariantService;


    @PostMapping
    public ResponseEntity<ProductVariantEntity> create(@RequestBody CreateProductVariantRequest request) {
        ProductVariantEntity variant  = productVariantService.create(request);
    return ResponseEntity.ok(variant);
    }
}
