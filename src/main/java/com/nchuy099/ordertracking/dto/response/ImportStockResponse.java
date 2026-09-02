package com.nchuy099.ordertracking.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ImportStockResponse {
    private String inventoryId;
    private String warehouseId;
    private String productVariantId;
    private Integer quantityInStock;
}
