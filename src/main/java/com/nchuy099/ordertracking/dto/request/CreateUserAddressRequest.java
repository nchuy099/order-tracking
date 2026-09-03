package com.nchuy099.ordertracking.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateUserAddressRequest {
    private String recipientName;
    private String recipientPhone;
    private String province;
    private String district;
    private String ward;
    private String detailAddress;
}
