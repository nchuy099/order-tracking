package com.nchuy099.ordertracking.dto.response;

import com.nchuy099.ordertracking.common.ProductVariantStatusEnum;
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
public class ProductDetailResponse {

    private UUID productId;
    private String name;
    private String description;
    private String primaryImageUrl;
    private List<String> extraImageUrls;
    private UUID categoryId;
    private String categoryName;
    private List<ProductVariantResponse> variants;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductVariantResponse {
        private UUID productVariantId;
        private String sku;
        private String name;
        private BigDecimal price;
        private ProductVariantStatusEnum status;
    }
}
