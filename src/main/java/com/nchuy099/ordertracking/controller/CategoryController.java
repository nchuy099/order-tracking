package com.nchuy099.ordertracking.controller;

import com.nchuy099.ordertracking.dto.request.CreateCategoryRequest;
import com.nchuy099.ordertracking.dto.request.CreateUserRequest;
import com.nchuy099.ordertracking.entity.CategoryEntity;
import com.nchuy099.ordertracking.entity.UserEntity;
import com.nchuy099.ordertracking.dto.response.HighlightedCategoryResponse;
import com.nchuy099.ordertracking.service.CategoryService;
import com.nchuy099.ordertracking.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;


    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryEntity> create(@RequestBody CreateCategoryRequest request) {
    CategoryEntity category = categoryService.create(request);
    return ResponseEntity.ok(category);
    }

    @GetMapping("/highlighted")
    public ResponseEntity<List<HighlightedCategoryResponse>> getHighlighted(
            @RequestParam(defaultValue = "5") int size
    ) {
        return ResponseEntity.ok(categoryService.getHighlighted(size));
    }
}
