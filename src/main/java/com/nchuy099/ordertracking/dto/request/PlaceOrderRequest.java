package com.nchuy099.ordertracking.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlaceOrderRequest {
    private String userAddressId;
    private String recipientName;
    private String recipientPhone;
    private String shippingAddress;
    private String discountCode;
    private String paymentMethod;
    private String note;
}
