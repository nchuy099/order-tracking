package com.nchuy099.ordertracking.controller;

import com.nchuy099.ordertracking.dto.request.CreateWarehouseRequest;
import com.nchuy099.ordertracking.entity.WarehouseEntity;
import com.nchuy099.ordertracking.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/warehouses")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService warehouseService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WarehouseEntity> create(@RequestBody CreateWarehouseRequest request) {
        WarehouseEntity warehouse = warehouseService.create(request);
        return ResponseEntity.ok(warehouse);
    }
}
