package com.nchuy099.ordertracking.service.impl;

import com.nchuy099.ordertracking.dto.request.CreateProductRequest;
import com.nchuy099.ordertracking.dto.response.ProductDetailResponse;
import com.nchuy099.ordertracking.dto.response.ProductListResponse;
import com.nchuy099.ordertracking.dto.response.ProductSummaryResponse;
import com.nchuy099.ordertracking.common.StockStatusEnum;
import com.nchuy099.ordertracking.entity.CategoryEntity;
import com.nchuy099.ordertracking.entity.ProductEntity;
import com.nchuy099.ordertracking.entity.ProductVariantEntity;
import com.nchuy099.ordertracking.exception.BusinessException;
import com.nchuy099.ordertracking.repository.CategoryRepository;
import com.nchuy099.ordertracking.repository.ProductRepository;
import com.nchuy099.ordertracking.repository.ProductSummaryProjection;
import com.nchuy099.ordertracking.repository.ProductVariantRepository;
import com.nchuy099.ordertracking.service.ProductService;
import com.nchuy099.ordertracking.service.spec.ProductSpecification;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor

public class ProductServiceImpl implements ProductService {

    private static final int MAX_PAGE_SIZE = 100;
    private static final int LIMITED_STOCK = 10;

    private final CategoryRepository categoryRepository;

    private final ProductRepository productRepository;

    private final ProductVariantRepository productVariantRepository;

    private final EntityManager entityManager;


    @Override
    public ProductEntity create(CreateProductRequest request) {
        //check cat

        Optional<CategoryEntity> catOpt = categoryRepository.findById(UUID.fromString(request.getCategoryId()));
        if (catOpt.isEmpty()) {
            throw new BusinessException("CATEGORY_NOT_FOUND",
                    "Category not found",
                    HttpStatus.NOT_FOUND);
        }

        //create product

        ProductEntity product = ProductEntity.builder()
                .name(request.getName())
                .description(request.getDescription())
                .primaryImageUrl(request.getPrimaryImageUrl())
                .extraImageUrls(request.getExtraImageUrls())
                .category(catOpt.get())
                .build();

        productRepository.save(product);
        return product;
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDetailResponse getDetails(UUID productId) {
        ProductEntity product = productRepository.findByIdWithCategory(productId)
                .orElseThrow(() -> new BusinessException(
                        "PRODUCT_NOT_FOUND",
                        "Product not found",
                        HttpStatus.NOT_FOUND
                ));

        List<ProductDetailResponse.ProductVariantResponse> variants = new ArrayList<>();
        for (ProductVariantEntity variant : productVariantRepository.findAllByProductId(productId)) {
            variants.add(ProductDetailResponse.ProductVariantResponse.builder()
                        .productVariantId(variant.getId())
                        .sku(variant.getSku())
                        .name(variant.getName())
                        .price(variant.getPrice())
                        .status(variant.getStatus())
                        .build());
        }

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

    @Override
    @Transactional(readOnly = true)
    public ProductListResponse search(
            String keyword,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String stockStatus,
            int page,
            int size
    ) {
        Pageable pageable = buildPageable(page, size);
        return toListResponse(findProducts(buildProductSpecification(keyword, minPrice, maxPrice, stockStatus), pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductListResponse getHighlighted(int page, int size) {
        return toListResponse(productRepository.findByDeleted(0, buildPageable(page, size)));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductSummaryResponse getSummary() {
        ProductSummaryProjection summary = productRepository.getSummary(LIMITED_STOCK);
        return ProductSummaryResponse.builder()
                .totalInventoryValue(summary.getTotalInventoryValue())
                .totalProductValue(summary.getTotalProductValue())
                .lowStockVariantCount(summary.getLowStockVariantCount())
                .outOfStockVariantCount(summary.getOutOfStockVariantCount())
                .build();
    }

    private Pageable buildPageable(int page, int size) {
        validatePageRequest(page, size);
        return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    private void validatePageRequest(int page, int size) {
        if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) {
            throw new BusinessException(
                    "INVALID_PAGE_REQUEST",
                    "Page must be zero or greater and size must be between 1 and 100",
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    private void validatePriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        if ((minPrice != null && minPrice.signum() < 0)
                || (maxPrice != null && maxPrice.signum() < 0)
                || (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0)) {
            throw new BusinessException(
                    "INVALID_PRICE_RANGE",
                    "Price range must be non-negative and minPrice must not exceed maxPrice",
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    private Page<ProductEntity> findProducts(Specification<ProductEntity> specification, Pageable pageable) {
        List<ProductEntity> products = findProductBatch(
                specification,
                Math.toIntExact(pageable.getOffset()),
                pageable.getPageSize()
        );

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

        CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);
        Root<ProductEntity> countRoot = countQuery.from(ProductEntity.class);
        Predicate countPredicate = specification.toPredicate(countRoot, countQuery, criteriaBuilder);
        countQuery.select(criteriaBuilder.count(countRoot)).where(countPredicate);

        long totalElements = entityManager.createQuery(countQuery).getSingleResult();
        return new PageImpl<>(products, pageable, totalElements);
    }

    private List<ProductEntity> findProductBatch(
            Specification<ProductEntity> specification,
            int offset,
            int limit
    ) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<ProductEntity> productQuery = criteriaBuilder.createQuery(ProductEntity.class);
        Root<ProductEntity> productRoot = productQuery.from(ProductEntity.class);
        productRoot.fetch("category", JoinType.LEFT);
        Predicate productPredicate = specification.toPredicate(productRoot, productQuery, criteriaBuilder);

        productQuery.select(productRoot)
                .where(productPredicate)
                .orderBy(criteriaBuilder.desc(productRoot.get("createdAt")));

        return entityManager.createQuery(productQuery)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    private Specification<ProductEntity> buildProductSpecification(
            String keyword,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String stockStatus
    ) {
        validatePriceRange(minPrice, maxPrice);
        Specification<ProductEntity> specification = ProductSpecification.isNotDeleted();

        if (keyword != null && !keyword.isBlank()) {
            specification = specification.and(ProductSpecification.hasNameContaining(keyword.trim()));
        }
        if (minPrice != null || maxPrice != null) {
            specification = specification.and(ProductSpecification.hasVariantPriceBetween(minPrice, maxPrice));
        }
        StockQuantityRange stockQuantityRange = getStockQuantityRange(stockStatus);
        if (stockQuantityRange != null) {
            specification = specification.and(ProductSpecification.hasVariantStockBetween(
                    stockQuantityRange.minQuantityInStock,
                    stockQuantityRange.maxQuantityInStock
            ));
        }
        return specification;
    }

    private StockQuantityRange getStockQuantityRange(String stockStatus) {
        if (stockStatus == null || stockStatus.isBlank()) {
            return null;
        }

        StockStatusEnum parsedStockStatus = StockStatusEnum.fromValue(stockStatus);
        if (parsedStockStatus == null) {
            throw new BusinessException(
                    "INVALID_STOCK_STATUS",
                    "Stock status must be IN_STOCK, LIMITED_STOCK, or OUT_OF_STOCK",
                    HttpStatus.BAD_REQUEST
            );
        }

        if (parsedStockStatus == StockStatusEnum.IN_STOCK) {
            return new StockQuantityRange(LIMITED_STOCK, null);
        }
        if (parsedStockStatus == StockStatusEnum.LIMITED_STOCK) {
            return new StockQuantityRange(1, LIMITED_STOCK - 1);
        }
        return new StockQuantityRange(0, 0);
    }

    private static class StockQuantityRange {
        private final Integer minQuantityInStock;
        private final Integer maxQuantityInStock;

        private StockQuantityRange(Integer minQuantityInStock, Integer maxQuantityInStock) {
            this.minQuantityInStock = minQuantityInStock;
            this.maxQuantityInStock = maxQuantityInStock;
        }
    }

    private ProductListResponse toListResponse(Page<ProductEntity> products) {
        List<ProductListResponse.ProductResponse> content = new ArrayList<>();

        for (ProductEntity product : products.getContent()) {
            content.add(ProductListResponse.ProductResponse.builder()
                    .productId(product.getId())
                    .name(product.getName())
                    .primaryImageUrl(product.getPrimaryImageUrl())
                    .categoryId(product.getCategory() == null ? null : product.getCategory().getId())
                    .categoryName(product.getCategory() == null ? null : product.getCategory().getName())
                    .build());
        }

        return ProductListResponse.builder()
                .content(content)
                .page(products.getNumber())
                .size(products.getSize())
                .totalElements(products.getTotalElements())
                .totalPages(products.getTotalPages())
                .build();
    }

}
