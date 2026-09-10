package com.nchuy099.ordertracking.service;

import com.nchuy099.ordertracking.dto.request.CreateCategoryRequest;
import com.nchuy099.ordertracking.entity.CategoryEntity;
import com.nchuy099.ordertracking.dto.response.HighlightedCategoryResponse;

import java.util.List;

public interface CategoryService {

    CategoryEntity create(CreateCategoryRequest request);

    List<HighlightedCategoryResponse> getHighlighted(int size);
}
