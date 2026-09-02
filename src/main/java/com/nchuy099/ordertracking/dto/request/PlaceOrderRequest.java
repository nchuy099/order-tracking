package com.nchuy099.ordertracking.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PlaceOrderRequest {
    private String recipientName;
    private String recipientPhone;
    private String shippingAddress;
    private String discountCode;
    private BigDecimal shippingFee;
    private String paymentMethod;
    private String note;
}
