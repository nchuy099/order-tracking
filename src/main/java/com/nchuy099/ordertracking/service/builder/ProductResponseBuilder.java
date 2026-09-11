package com.nchuy099.ordertracking.service.builder;

import com.nchuy099.ordertracking.common.StockStatusEnum;
import com.nchuy099.ordertracking.dto.response.ProductDetailResponse;
import com.nchuy099.ordertracking.dto.response.ProductVariantExportRow;
import com.nchuy099.ordertracking.dto.response.ProductListResponse;
import com.nchuy099.ordertracking.dto.response.ProductSummaryResponse;
import com.nchuy099.ordertracking.dto.response.ProductVariantListResponse;
import com.nchuy099.ordertracking.entity.ProductEntity;
import com.nchuy099.ordertracking.entity.ProductVariantEntity;
import com.nchuy099.ordertracking.repository.ProductSummaryProjection;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public final class ProductResponseBuilder {

    private ProductResponseBuilder() {
    }

    public static ProductDetailResponse buildProductDetail(
            ProductEntity product,
            List<ProductDetailResponse.ProductVariantResponse> variants
    ) {
        return ProductDetailResponse.builder()
                .productId(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .primaryImageUrl(product.getPrimaryImageUrl())
                .extraImageUrls(product.getExtraImageUrls())
                .categoryId(product.getCategory() == null ? null : product.getCategory().getId())
                .categoryName(product.getCategory() == null ? null : product.getCategory().getName())
                .variants(variants)
                .build();
    }

    public static ProductDetailResponse.ProductVariantResponse buildProductDetailVariant(
            ProductVariantEntity variant
    ) {
        return ProductDetailResponse.ProductVariantResponse.builder()
                .productVariantId(variant.getId())
                .sku(variant.getSku())
                .name(variant.getName())
                .price(variant.getPrice())
                .status(variant.getStatus())
                .build();
    }

    public static ProductVariantListResponse buildProductVariantList(
            List<ProductVariantListResponse.ProductVariantResponse> content,
            int page,
            int size,
            long totalElements
    ) {
        return ProductVariantListResponse.builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages((int) Math.ceil((double) totalElements / size))
                .build();
    }

    public static ProductSummaryResponse buildProductSummary(ProductSummaryProjection summary) {
        return ProductSummaryResponse.builder()
                .totalInventoryValue(summary.getTotalInventoryValue())
                .totalProductValue(summary.getTotalProductValue())
                .lowStockVariantCount(summary.getLowStockVariantCount())
                .outOfStockVariantCount(summary.getOutOfStockVariantCount())
                .build();
    }

    public static ProductListResponse buildProductList(
            Page<ProductEntity> products,
            List<ProductListResponse.ProductResponse> content
    ) {
        return ProductListResponse.builder()
                .content(content)
                .page(products.getNumber())
                .size(products.getSize())
                .totalElements(products.getTotalElements())
                .totalPages(products.getTotalPages())
                .build();
    }

    public static ProductListResponse.ProductResponse buildProductListItem(ProductEntity product) {
        return ProductListResponse.ProductResponse.builder()
                .productId(product.getId())
                .name(product.getName())
                .primaryImageUrl(product.getPrimaryImageUrl())
                .categoryId(product.getCategory() == null ? null : product.getCategory().getId())
                .categoryName(product.getCategory() == null ? null : product.getCategory().getName())
                .build();
    }

    public static ProductVariantListResponse.ProductVariantResponse buildProductVariantListItem(
            UUID productId,
            String productName,
            String primaryImageUrl,
            UUID categoryId,
            String categoryName,
            UUID productVariantId,
            String variantName,
            String sku,
            BigDecimal price,
            Long totalQuantityInStock,
            StockStatusEnum stockStatus
    ) {
        return ProductVariantListResponse.ProductVariantResponse.builder()
                .productId(productId)
                .productName(productName)
                .primaryImageUrl(primaryImageUrl)
                .categoryId(categoryId)
                .categoryName(categoryName)
                .productVariantId(productVariantId)
                .variantName(variantName)
                .sku(sku)
                .price(price)
                .totalQuantityInStock(totalQuantityInStock)
                .stockStatus(stockStatus)
                .build();
    }

    public static ProductVariantExportRow buildProductVariantExportRow(
            UUID productId,
            String productName,
            UUID categoryId,
            String categoryName,
            String primaryImageUrl,
            UUID productVariantId,
            String variantName,
            String sku,
            BigDecimal price,
            Long totalQuantityInStock,
            String stockStatus
    ) {
        return ProductVariantExportRow.builder()
                .productId(productId == null ? null : productId.toString())
                .productName(productName)
                .categoryId(categoryId == null ? null : categoryId.toString())
                .categoryName(categoryName)
                .primaryImageUrl(primaryImageUrl)
                .productVariantId(productVariantId == null ? null : productVariantId.toString())
                .variantName(variantName)
                .sku(sku)
                .price(price)
                .totalQuantityInStock(totalQuantityInStock)
                .stockStatus(stockStatus)
                .build();
    }
}
