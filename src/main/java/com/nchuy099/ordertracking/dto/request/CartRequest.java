package com.nchuy099.ordertracking.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartRequest {
    private String productVariantId;
    private Integer quantity;
}
