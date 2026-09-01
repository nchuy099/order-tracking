package com.nchuy099.ordertracking.service;

import com.nchuy099.ordertracking.dto.request.CreateProductRequest;
import com.nchuy099.ordertracking.dto.request.CreateProductVariantRequest;
import com.nchuy099.ordertracking.entity.ProductEntity;
import com.nchuy099.ordertracking.entity.ProductVariantEntity;

public interface ProductVariantService {

    ProductVariantEntity create(CreateProductVariantRequest request);
}
