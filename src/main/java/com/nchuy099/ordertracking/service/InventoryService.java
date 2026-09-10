package com.nchuy099.ordertracking.service;

import com.nchuy099.ordertracking.dto.request.ImportStockRequest;
import com.nchuy099.ordertracking.dto.response.ImportStockResponse;
import com.nchuy099.ordertracking.dto.response.ProductVariantInventoryResponse;

import java.util.UUID;

public interface InventoryService {

    void importStock(ImportStockRequest request);

    ProductVariantInventoryResponse getByProductVariantId(UUID productVariantId);
}
