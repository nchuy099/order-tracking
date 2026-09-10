package com.nchuy099.ordertracking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSummaryResponse {

    private BigDecimal totalInventoryValue;
    private BigDecimal totalProductValue;
    private Long lowStockVariantCount;
    private Long outOfStockVariantCount;
}
