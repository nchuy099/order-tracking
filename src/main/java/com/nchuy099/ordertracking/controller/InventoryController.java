package com.nchuy099.ordertracking.controller;

import com.nchuy099.ordertracking.dto.request.ImportStockRequest;
import com.nchuy099.ordertracking.dto.response.ImportStockResponse;
import com.nchuy099.ordertracking.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/inventories")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping("/import-stock")
    public ResponseEntity<ImportStockResponse> importStock(@RequestBody ImportStockRequest request) {
        ImportStockResponse response = inventoryService.importStock(request);
        return ResponseEntity.ok(response);
    }
}
