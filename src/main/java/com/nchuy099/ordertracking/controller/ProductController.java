package com.nchuy099.ordertracking.controller;

import com.nchuy099.ordertracking.dto.request.CreateCategoryRequest;
import com.nchuy099.ordertracking.dto.request.CreateProductRequest;
import com.nchuy099.ordertracking.dto.response.ProductDetailResponse;
import com.nchuy099.ordertracking.dto.response.ProductVariantInventoryResponse;
import com.nchuy099.ordertracking.dto.response.ProductListResponse;
import com.nchuy099.ordertracking.dto.response.ProductSummaryResponse;
import com.nchuy099.ordertracking.entity.CategoryEntity;
import com.nchuy099.ordertracking.entity.ProductEntity;
import com.nchuy099.ordertracking.service.CategoryService;
import com.nchuy099.ordertracking.service.InventoryService;
import com.nchuy099.ordertracking.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final InventoryService inventoryService;


    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductEntity> create(@RequestBody CreateProductRequest request) {
        ProductEntity product  = productService.create(request);
    return ResponseEntity.ok(product);
    }

    @GetMapping("/{productId}/details")
    public ResponseEntity<ProductDetailResponse> getDetails(@PathVariable UUID productId) {
        return ResponseEntity.ok(productService.getDetails(productId));
    }

    @GetMapping("/variants/{productVariantId}/inventories")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductVariantInventoryResponse> getInventories(
            @PathVariable UUID productVariantId
    ) {
        return ResponseEntity.ok(inventoryService.getByProductVariantId(productVariantId));
    }

    @GetMapping
    public ResponseEntity<ProductListResponse> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String stockStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(productService.search(keyword, minPrice, maxPrice, stockStatus, page, size));
    }

    @GetMapping("/highlighted")
    public ResponseEntity<ProductListResponse> getHighlighted(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "4") int size
    ) {
        return ResponseEntity.ok(productService.getHighlighted(page, size));
    }

    @GetMapping("/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductSummaryResponse> getSummary() {
        return ResponseEntity.ok(productService.getSummary());
    }
}
