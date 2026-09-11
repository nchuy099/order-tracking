package com.nchuy099.ordertracking.dto.response;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariantExportRow {

    @ExcelProperty("Product ID")
    private String productId;

    @ExcelProperty("Tên sản phẩm")
    private String productName;

    @ExcelProperty("Category ID")
    private String categoryId;

    @ExcelProperty("Tên category")
    private String categoryName;

    @ExcelProperty("URL ảnh chính")
    private String primaryImageUrl;

    @ExcelProperty("Variant ID")
    private String productVariantId;

    @ExcelProperty("Tên variant")
    private String variantName;

    @ExcelProperty("SKU")
    private String sku;

    @ExcelProperty("Giá")
    private BigDecimal price;

    @ExcelProperty("Tổng tồn kho")
    private Long totalQuantityInStock;

    @ExcelProperty("Trạng thái tồn kho")
    private String stockStatus;
}
