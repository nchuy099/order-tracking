package com.nchuy099.ordertracking.repository;

import java.math.BigDecimal;

public interface ProductSummaryProjection {

    BigDecimal getTotalInventoryValue();

    BigDecimal getTotalProductValue();

    Long getLowStockVariantCount();

    Long getOutOfStockVariantCount();
}
