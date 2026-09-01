package com.nchuy099.ordertracking.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class CreateProductVariantRequest {
    private String productId;
    private String sku;
    private String name;
    private BigDecimal price;
    private String status;

}