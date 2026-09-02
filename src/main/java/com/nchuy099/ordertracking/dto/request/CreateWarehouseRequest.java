package com.nchuy099.ordertracking.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateWarehouseRequest {
    private String code;
    private String name;
    private String province;
    private String district;
    private String ward;
    private String addressLine;
    private Boolean active;
}
