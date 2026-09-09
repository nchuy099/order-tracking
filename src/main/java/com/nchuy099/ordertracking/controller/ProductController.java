package com.nchuy099.ordertracking.controller;

import com.nchuy099.ordertracking.dto.request.CreateCategoryRequest;
import com.nchuy099.ordertracking.dto.request.CreateProductRequest;
import com.nchuy099.ordertracking.entity.CategoryEntity;
import com.nchuy099.ordertracking.entity.ProductEntity;
import com.nchuy099.ordertracking.service.CategoryService;
import com.nchuy099.ordertracking.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;


    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductEntity> create(@RequestBody CreateProductRequest request) {
        ProductEntity product  = productService.create(request);
    return ResponseEntity.ok(product);
    }
}
