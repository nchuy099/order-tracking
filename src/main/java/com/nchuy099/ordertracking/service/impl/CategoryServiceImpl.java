package com.nchuy099.ordertracking.service.impl;

import com.nchuy099.ordertracking.dto.request.CreateCategoryRequest;
import com.nchuy099.ordertracking.dto.response.HighlightedCategoryResponse;
import com.nchuy099.ordertracking.entity.CategoryEntity;
import com.nchuy099.ordertracking.exception.BusinessException;
import com.nchuy099.ordertracking.repository.CategoryRepository;
import com.nchuy099.ordertracking.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private static final int MAX_HIGHLIGHTED_SIZE = 100;

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

    @Override
    @Transactional(readOnly = true)
    public List<HighlightedCategoryResponse> getHighlighted(int size) {
        if (size < 1 || size > MAX_HIGHLIGHTED_SIZE) {
            throw new BusinessException(
                    "INVALID_SIZE",
                    "Size must be between 1 and 100",
                    HttpStatus.BAD_REQUEST
            );
        }

        List<HighlightedCategoryResponse> categories = new ArrayList<>();
        List<CategoryEntity> highlightedCategories = categoryRepository.findByActiveTrueAndDeleted(
                0,
                PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        );

        for (CategoryEntity category : highlightedCategories) {
            categories.add(HighlightedCategoryResponse.builder()
                    .categoryId(category.getId())
                    .name(category.getName())
                    .description(category.getDescription())
                    .build());
        }

        return categories;
    }
}
