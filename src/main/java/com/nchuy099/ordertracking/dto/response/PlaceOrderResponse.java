package com.nchuy099.ordertracking.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class PlaceOrderResponse {
    private String orderId;
    private String orderCode;
    private String paymentId;
    private String paymentCode;
    private BigDecimal grandTotal;
}
