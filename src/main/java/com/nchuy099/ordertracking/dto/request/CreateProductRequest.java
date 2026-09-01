package com.nchuy099.ordertracking.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateProductRequest {
    private String categoryId;
    private String name;
    private String description;
    private String primaryImageUrl;
    private List<String> extraImageUrls;

}