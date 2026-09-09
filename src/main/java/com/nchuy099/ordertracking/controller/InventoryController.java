package com.nchuy099.ordertracking.controller;

import com.nchuy099.ordertracking.dto.request.ImportStockRequest;
import com.nchuy099.ordertracking.dto.response.ImportStockResponse;
import com.nchuy099.ordertracking.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inventories")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @PutMapping("/stock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> importStock(@RequestBody ImportStockRequest request) {
        inventoryService.importStock(request);
        return ResponseEntity.ok(null);
    }
}
