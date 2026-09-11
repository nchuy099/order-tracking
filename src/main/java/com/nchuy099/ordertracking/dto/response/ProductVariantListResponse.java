package com.nchuy099.ordertracking.dto.response;

import com.nchuy099.ordertracking.common.StockStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariantListResponse {

    private List<ProductVariantResponse> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductVariantResponse {
        private UUID productId;
        private String productName;
        private String primaryImageUrl;
        private UUID categoryId;
        private String categoryName;
        private UUID productVariantId;
        private String variantName;
        private String sku;
        private BigDecimal price;
        private Long totalQuantityInStock;
        private StockStatusEnum stockStatus;
    }
}
