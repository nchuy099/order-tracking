package com.nchuy099.ordertracking.service.impl;

import com.nchuy099.ordertracking.dto.request.CreateCategoryRequest;
import com.nchuy099.ordertracking.entity.CategoryEntity;
import com.nchuy099.ordertracking.exception.BusinessException;
import com.nchuy099.ordertracking.repository.CategoryRepository;
import com.nchuy099.ordertracking.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public CategoryEntity create(CreateCategoryRequest request) {
        Optional<CategoryEntity> parentCategoryOpt = Optional.empty();
        if (request.getParentId() != null) {
            parentCategoryOpt = categoryRepository.findById(UUID.fromString(request.getParentId()));

            if (parentCategoryOpt.isEmpty()) {
                throw new BusinessException("PARENT_CATEGORY_NOT_FOUND",
                        "Parent category not found",
                        HttpStatus.NOT_FOUND);
            }
        }
        CategoryEntity category = CategoryEntity.builder()
                .name(request.getName())
                .description(request.getDescription())
                .parent(parentCategoryOpt.orElse(null))
                .build();

        categoryRepository.save(category);
        return category;
    }
}
