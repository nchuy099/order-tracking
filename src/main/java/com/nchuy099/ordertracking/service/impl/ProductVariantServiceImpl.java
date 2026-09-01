package com.nchuy099.ordertracking.service.impl;

import com.nchuy099.ordertracking.common.ProductVariantStatusEnum;
import com.nchuy099.ordertracking.dto.request.CreateProductRequest;
import com.nchuy099.ordertracking.dto.request.CreateProductVariantRequest;
import com.nchuy099.ordertracking.entity.CategoryEntity;
import com.nchuy099.ordertracking.entity.ProductEntity;
import com.nchuy099.ordertracking.entity.ProductVariantEntity;
import com.nchuy099.ordertracking.exception.BusinessException;
import com.nchuy099.ordertracking.repository.CategoryRepository;
import com.nchuy099.ordertracking.repository.ProductRepository;
import com.nchuy099.ordertracking.repository.ProductVariantRepository;
import com.nchuy099.ordertracking.service.ProductService;
import com.nchuy099.ordertracking.service.ProductVariantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor

public class ProductVariantServiceImpl implements ProductVariantService {

    private final ProductRepository productRepository;

    private final ProductVariantRepository productVariantRepository;


    @Override
    public ProductVariantEntity create(CreateProductVariantRequest request) {
        //check cat

        Optional<ProductEntity> productOpt = productRepository.findById(UUID.fromString(request.getProductId()));
        if (productOpt.isEmpty()) {
            throw new BusinessException("PRODUCT_NOT_FOUND",
                    "Product not found",
                    HttpStatus.NOT_FOUND);
        }

        //create product

        ProductVariantEntity variant = ProductVariantEntity.builder()
                .name(request.getName())
                .sku(request.getSku())
                .price(request.getPrice())
                .product(productOpt.get())
                .build();

        productVariantRepository.save(variant);
        return variant;
    }

}
