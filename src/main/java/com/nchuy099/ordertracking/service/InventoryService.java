package com.nchuy099.ordertracking.service;

import com.nchuy099.ordertracking.dto.request.ImportStockRequest;
import com.nchuy099.ordertracking.dto.response.ImportStockResponse;

public interface InventoryService {

    void importStock(ImportStockRequest request);
}
