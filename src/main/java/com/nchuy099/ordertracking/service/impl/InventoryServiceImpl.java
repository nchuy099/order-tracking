package com.nchuy099.ordertracking.service.impl;

import com.nchuy099.ordertracking.dto.request.ImportStockRequest;
import com.nchuy099.ordertracking.dto.response.ImportStockResponse;
import com.nchuy099.ordertracking.entity.InventoryEntity;
import com.nchuy099.ordertracking.entity.ProductVariantEntity;
import com.nchuy099.ordertracking.entity.WarehouseEntity;
import com.nchuy099.ordertracking.exception.BusinessException;
import com.nchuy099.ordertracking.repository.InventoryRepository;
import com.nchuy099.ordertracking.repository.ProductVariantRepository;
import com.nchuy099.ordertracking.repository.WarehouseRepository;
import com.nchuy099.ordertracking.service.InventoryService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductVariantRepository productVariantRepository;
    private final WarehouseRepository warehouseRepository;

    @Transactional
    @Override
    public void importStock(ImportStockRequest request) {
        UUID warehouseId = UUID.fromString(request.getWarehouseId());
        UUID productVariantId = UUID.fromString(request.getProductVariantId());

        Optional<WarehouseEntity> warehouseOpt = warehouseRepository.findById(warehouseId);
        if (warehouseOpt.isEmpty()) {
            throw new BusinessException("WAREHOUSE_NOT_FOUND",
                    "Warehouse not found",
                    HttpStatus.NOT_FOUND);
        }

        Optional<ProductVariantEntity> productVariantOpt = productVariantRepository.findById(productVariantId);
        if (productVariantOpt.isEmpty()) {
            throw new BusinessException("PRODUCT_VARIANT_NOT_FOUND",
                    "Product Variant not found",
                    HttpStatus.NOT_FOUND);
        }

        InventoryEntity inventory = inventoryRepository
                .findByWarehouseIdAndProductVariantId(warehouseId, productVariantId)
                .orElseGet(() -> InventoryEntity.builder()
                        .warehouse(warehouseOpt.get())
                        .productVariant(productVariantOpt.get())
                        .quantityInStock(0)
                        .build());

        inventory.setQuantityInStock(inventory.getQuantityInStock() + request.getQuantity());
        inventoryRepository.save(inventory);
    }
}
