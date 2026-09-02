package com.nchuy099.ordertracking.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class OrderSummaryResponse {
    private BigDecimal subTotal;
    private BigDecimal discountAmount;
    private BigDecimal shippingFee;
    private BigDecimal grandTotal;
}
