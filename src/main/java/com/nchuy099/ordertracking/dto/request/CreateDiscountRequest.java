package com.nchuy099.ordertracking.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class CreateDiscountRequest {
    private String code;
    private String name;
    private String description;
    private String type;
    private BigDecimal value;
    private BigDecimal maxDiscountAmount;
    private Integer usageLimit;
    private Integer usageLimitPerUser;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private String status;
}
