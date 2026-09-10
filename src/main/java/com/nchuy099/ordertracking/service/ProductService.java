package com.nchuy099.ordertracking.service;

import com.nchuy099.ordertracking.dto.request.CreateProductRequest;
import com.nchuy099.ordertracking.dto.response.ProductDetailResponse;
import com.nchuy099.ordertracking.dto.response.ProductListResponse;
import com.nchuy099.ordertracking.dto.response.ProductSummaryResponse;
import com.nchuy099.ordertracking.entity.ProductEntity;

import java.math.BigDecimal;
import java.util.UUID;

public interface ProductService {

    ProductEntity create(CreateProductRequest request);

    ProductDetailResponse getDetails(UUID productId);

    ProductListResponse search(
            String keyword,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String stockStatus,
            int page,
            int size
    );

    ProductListResponse getHighlighted(int page, int size);

    ProductSummaryResponse getSummary();
}
