package com.nchuy099.ordertracking.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImportStockRequest {
    private String warehouseId;
    private String productVariantId;
    private Integer quantity;
}
