package com.nchuy099.ordertracking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HighlightedCategoryResponse {

    private UUID categoryId;
    private String name;
    private String description;
}
