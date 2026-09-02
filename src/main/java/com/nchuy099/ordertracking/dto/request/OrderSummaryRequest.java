package com.nchuy099.ordertracking.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class OrderSummaryRequest {
    private String discountCode;
    private BigDecimal shippingFee;
}
