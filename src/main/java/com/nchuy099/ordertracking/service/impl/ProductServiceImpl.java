package com.nchuy099.ordertracking.service.impl;

import com.nchuy099.ordertracking.dto.request.CreateProductRequest;
import com.nchuy099.ordertracking.entity.CategoryEntity;
import com.nchuy099.ordertracking.entity.ProductEntity;
import com.nchuy099.ordertracking.exception.BusinessException;
import com.nchuy099.ordertracking.repository.CategoryRepository;
import com.nchuy099.ordertracking.repository.ProductRepository;
import com.nchuy099.ordertracking.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor

public class ProductServiceImpl implements ProductService {

    private final CategoryRepository categoryRepository;

    private final ProductRepository productRepository;


    @Override
    public ProductEntity create(CreateProductRequest request) {
        //check cat

        Optional<CategoryEntity> catOpt = categoryRepository.findById(UUID.fromString(request.getCategoryId()));
        if (catOpt.isEmpty()) {
            throw new BusinessException("CATEGORY_NOT_FOUND",
                    "Category not found",
                    HttpStatus.NOT_FOUND);
        }

        //create product

        ProductEntity product = ProductEntity.builder()
                .name(request.getName())
                .description(request.getDescription())
                .primaryImageUrl(request.getPrimaryImageUrl())
                .extraImageUrls(request.getExtraImageUrls())
                .category(catOpt.get())
                .build();

        productRepository.save(product);
        return product;
    }

}
