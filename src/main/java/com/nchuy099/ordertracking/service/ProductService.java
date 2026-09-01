package com.nchuy099.ordertracking.service;

import com.nchuy099.ordertracking.dto.request.CreateProductRequest;
import com.nchuy099.ordertracking.entity.ProductEntity;

public interface ProductService {

    ProductEntity create(CreateProductRequest request);
}
