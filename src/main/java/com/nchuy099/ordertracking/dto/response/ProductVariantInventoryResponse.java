package com.nchuy099.ordertracking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariantInventoryResponse {

    private UUID productVariantId;
    private String sku;
    private String variantName;
    private Integer totalQuantityInStock;
    private List<WarehouseInventoryResponse> inventories;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WarehouseInventoryResponse {
        private UUID inventoryId;
        private UUID warehouseId;
        private String warehouseCode;
        private String warehouseName;
        private Boolean warehouseIsActive;
        private Integer quantityInStock;
    }
}
