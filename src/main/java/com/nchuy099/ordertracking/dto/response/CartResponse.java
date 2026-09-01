package com.nchuy099.ordertracking.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
public class CartResponse {
    List<CartItemResponse> cartItems;

    @Getter
    @Setter
    @Builder
    public static class CartItemResponse {

        private String productId;
        private String productName;
        private String productPrimaryImageUrl;
        private String productVariantSku;
        private Integer quantity;
        private BigDecimal price;
    }
}
