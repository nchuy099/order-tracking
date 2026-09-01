package com.nchuy099.ordertracking.service;

import com.nchuy099.ordertracking.dto.request.CreateCategoryRequest;
import com.nchuy099.ordertracking.entity.CategoryEntity;

public interface CategoryService {

    CategoryEntity create(CreateCategoryRequest request);
}
