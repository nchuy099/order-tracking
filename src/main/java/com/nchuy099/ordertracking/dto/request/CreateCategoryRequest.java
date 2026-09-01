package com.nchuy099.ordertracking.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateCategoryRequest {
    private String parentId;
    private String name;
    private String description;
    private Boolean active;

}