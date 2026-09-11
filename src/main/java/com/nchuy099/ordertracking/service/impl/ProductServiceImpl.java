package com.nchuy099.ordertracking.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.nchuy099.ordertracking.common.ExportFileFormatEnum;
import com.nchuy099.ordertracking.dto.request.CreateProductRequest;
import com.nchuy099.ordertracking.dto.response.ProductDetailResponse;
import com.nchuy099.ordertracking.dto.response.ProductVariantExportRow;
import com.nchuy099.ordertracking.dto.response.ProductVariantListResponse;
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
import com.nchuy099.ordertracking.service.builder.ProductResponseBuilder;
import com.nchuy099.ordertracking.service.spec.ProductSpecification;
import com.nchuy099.ordertracking.service.spec.ProductVariantSpecification;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor

public class ProductServiceImpl implements ProductService {

    private static final int MAX_PAGE_SIZE = 100;
    private static final int LIMITED_STOCK = 10;
    private static final int EXPORT_BATCH_SIZE = 500;

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
            variants.add(ProductResponseBuilder.buildProductDetailVariant(variant));
        }

        return ProductResponseBuilder.buildProductDetail(product, variants);
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
    public ProductVariantListResponse searchVariants(
            String keyword,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String stockStatus,
            int page,
            int size
    ) {
        Pageable pageable = buildVariantPageable(page, size);
        Specification<ProductVariantEntity> specification = buildProductVariantSpecification(
                keyword, minPrice, maxPrice, stockStatus
        );

        List<ProductVariantData> variants = findProductVariantBatch(
                specification,
                Math.toIntExact(pageable.getOffset()),
                pageable.getPageSize()
        );
        long totalElements = countProductVariants(specification);

        List<ProductVariantListResponse.ProductVariantResponse> content = new ArrayList<>();
        for (ProductVariantData variant : variants) {
            content.add(toProductVariantResponse(variant));
        }

        return ProductResponseBuilder.buildProductVariantList(
                content,
                pageable.getPageNumber(),
                pageable.getPageSize(),
                totalElements
        );
    }

    @Override
    @Transactional(readOnly = true)
    public void exportVariants(
            String keyword,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String stockStatus,
            ExportFileFormatEnum exportFormat,
            OutputStream outputStream
    ) throws IOException {
        Specification<ProductVariantEntity> specification = buildProductVariantSpecification(
                keyword, minPrice, maxPrice, stockStatus
        );

        if (exportFormat == ExportFileFormatEnum.CSV) {
            exportVariantsToCsv(specification, outputStream);
            return;
        }

        exportVariantsToXlsx(specification, outputStream);
    }

    private void exportVariantsToXlsx(
            Specification<ProductVariantEntity> specification,
            OutputStream outputStream
    ) {
        try (ExcelWriter excelWriter = EasyExcel.write(outputStream, ProductVariantExportRow.class)
                .autoCloseStream(false)
                .build()) {
            WriteSheet writeSheet = EasyExcel.writerSheet("Product Variants").build();
            int offset = 0;

            while (true) {
                List<ProductVariantData> variants = findProductVariantBatch(
                        specification, offset, EXPORT_BATCH_SIZE
                );
                if (variants.isEmpty()) {
                    if (offset == 0) {
                        excelWriter.write(List.of(), writeSheet);
                    }
                    break;
                }

                List<ProductVariantExportRow> rows = new ArrayList<>();
                for (ProductVariantData variant : variants) {
                    rows.add(ProductResponseBuilder.buildProductVariantExportRow(
                            variant.productId,
                            variant.productName,
                            variant.categoryId,
                            variant.categoryName,
                            variant.primaryImageUrl,
                            variant.productVariantId,
                            variant.variantName,
                            variant.sku,
                            variant.price,
                            variant.totalQuantityInStock,
                            getStockStatus(variant.totalQuantityInStock).getValue()
                    ));
                }
                excelWriter.write(rows, writeSheet);

                if (variants.size() < EXPORT_BATCH_SIZE) {
                    break;
                }
                offset += EXPORT_BATCH_SIZE;
            }
        }
    }

    private void exportVariantsToCsv(
            Specification<ProductVariantEntity> specification,
            OutputStream outputStream
    ) throws IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
        writer.write('\uFEFF');
        writeCsvRow(writer, List.of(
                "Product ID", "Tên sản phẩm", "Category ID", "Tên category", "URL ảnh chính",
                "Variant ID", "Tên variant", "SKU", "Giá", "Tổng tồn kho", "Trạng thái tồn kho"
        ));

        int offset = 0;
        while (true) {
            List<ProductVariantData> variants = findProductVariantBatch(specification, offset, EXPORT_BATCH_SIZE);
            if (variants.isEmpty()) {
                break;
            }

            for (ProductVariantData variant : variants) {
                ProductVariantExportRow row = ProductResponseBuilder.buildProductVariantExportRow(
                        variant.productId,
                        variant.productName,
                        variant.categoryId,
                        variant.categoryName,
                        variant.primaryImageUrl,
                        variant.productVariantId,
                        variant.variantName,
                        variant.sku,
                        variant.price,
                        variant.totalQuantityInStock,
                        getStockStatus(variant.totalQuantityInStock).getValue()
                );
                writeCsvRow(writer, Arrays.asList(
                        row.getProductId(), row.getProductName(), row.getCategoryId(), row.getCategoryName(),
                        row.getPrimaryImageUrl(), row.getProductVariantId(), row.getVariantName(), row.getSku(),
                        row.getPrice() == null ? null : row.getPrice().toPlainString(),
                        row.getTotalQuantityInStock() == null ? null : row.getTotalQuantityInStock().toString(),
                        row.getStockStatus()
                ));
            }

            if (variants.size() < EXPORT_BATCH_SIZE) {
                break;
            }
            offset += EXPORT_BATCH_SIZE;
        }
        writer.flush();
    }

    private void writeCsvRow(BufferedWriter writer, List<String> values) throws IOException {
        for (int index = 0; index < values.size(); index++) {
            if (index > 0) {
                writer.write(',');
            }
            String value = values.get(index);
            if (value != null) {
                writer.write('"');
                writer.write(value.replace("\"", "\"\""));
                writer.write('"');
            }
        }
        writer.newLine();
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
        return ProductResponseBuilder.buildProductSummary(summary);
    }

    private Pageable buildPageable(int page, int size) {
        validatePageRequest(page, size);
        return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    private Pageable buildVariantPageable(int page, int size) {
        if (page < 0 || size < 1) {
            throw new BusinessException(
                    "INVALID_PAGE_REQUEST",
                    "Page must be zero or greater and size must be greater than zero",
                    HttpStatus.BAD_REQUEST
            );
        }
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

    private List<ProductVariantData> findProductVariantBatch(
            Specification<ProductVariantEntity> specification,
            int offset,
            int limit
    ) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = criteriaBuilder.createTupleQuery();
        Root<ProductVariantEntity> variant = query.from(ProductVariantEntity.class);
        var product = variant.join("product", JoinType.INNER);
        var category = product.join("category", JoinType.LEFT);
        var inventory = variant.join("inventories", JoinType.LEFT);
        inventory.on(criteriaBuilder.equal(inventory.get("deleted"), 0));

        var totalQuantityInStock = criteriaBuilder.coalesce(
                criteriaBuilder.sumAsLong(inventory.get("quantityInStock")),
                0L
        );
        Predicate predicate = specification.toPredicate(variant, query, criteriaBuilder);

        query.multiselect(
                        product.get("id").alias("productId"),
                        product.get("name").alias("productName"),
                        product.get("primaryImageUrl").alias("primaryImageUrl"),
                        category.get("id").alias("categoryId"),
                        category.get("name").alias("categoryName"),
                        variant.get("id").alias("productVariantId"),
                        variant.get("name").alias("variantName"),
                        variant.get("sku").alias("sku"),
                        variant.get("price").alias("price"),
                        totalQuantityInStock.alias("totalQuantityInStock")
                )
                .where(predicate)
                .groupBy(
                        product.get("id"),
                        product.get("name"),
                        product.get("primaryImageUrl"),
                        category.get("id"),
                        category.get("name"),
                        variant.get("id"),
                        variant.get("name"),
                        variant.get("sku"),
                        variant.get("price"),
                        variant.get("createdAt")
                )
                .orderBy(criteriaBuilder.desc(variant.get("createdAt")));

        List<Tuple> tuples = entityManager.createQuery(query)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
        List<ProductVariantData> variants = new ArrayList<>();
        for (Tuple tuple : tuples) {
            variants.add(new ProductVariantData(
                    tuple.get("productId", UUID.class),
                    tuple.get("productName", String.class),
                    tuple.get("primaryImageUrl", String.class),
                    tuple.get("categoryId", UUID.class),
                    tuple.get("categoryName", String.class),
                    tuple.get("productVariantId", UUID.class),
                    tuple.get("variantName", String.class),
                    tuple.get("sku", String.class),
                    tuple.get("price", BigDecimal.class),
                    tuple.get("totalQuantityInStock", Long.class)
            ));
        }
        return variants;
    }

    private long countProductVariants(Specification<ProductVariantEntity> specification) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);
        Root<ProductVariantEntity> variant = countQuery.from(ProductVariantEntity.class);
        Predicate predicate = specification.toPredicate(variant, countQuery, criteriaBuilder);

        countQuery.select(criteriaBuilder.count(variant)).where(predicate);
        return entityManager.createQuery(countQuery).getSingleResult();
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

    private Specification<ProductVariantEntity> buildProductVariantSpecification(
            String keyword,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String stockStatus
    ) {
        validatePriceRange(minPrice, maxPrice);
        Specification<ProductVariantEntity> specification = ProductVariantSpecification.isActiveAndNotDeleted();

        if (keyword != null && !keyword.isBlank()) {
            specification = specification.and(ProductVariantSpecification.hasKeyword(keyword.trim()));
        }
        if (minPrice != null || maxPrice != null) {
            specification = specification.and(ProductVariantSpecification.hasPriceBetween(minPrice, maxPrice));
        }

        StockQuantityRange stockQuantityRange = getStockQuantityRange(stockStatus);
        if (stockQuantityRange != null) {
            specification = specification.and(ProductVariantSpecification.hasTotalStockBetween(
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

    private StockStatusEnum getStockStatus(Long totalQuantityInStock) {
        long quantity = totalQuantityInStock == null ? 0L : totalQuantityInStock;
        if (quantity <= 0) {
            return StockStatusEnum.OUT_OF_STOCK;
        }
        if (quantity < LIMITED_STOCK) {
            return StockStatusEnum.LIMITED_STOCK;
        }
        return StockStatusEnum.IN_STOCK;
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
            content.add(ProductResponseBuilder.buildProductListItem(product));
        }

        return ProductResponseBuilder.buildProductList(products, content);
    }

    private ProductVariantListResponse.ProductVariantResponse toProductVariantResponse(ProductVariantData variant) {
        return ProductResponseBuilder.buildProductVariantListItem(
                variant.productId,
                variant.productName,
                variant.primaryImageUrl,
                variant.categoryId,
                variant.categoryName,
                variant.productVariantId,
                variant.variantName,
                variant.sku,
                variant.price,
                variant.totalQuantityInStock,
                getStockStatus(variant.totalQuantityInStock)
        );
    }

    private static class ProductVariantData {
        private final UUID productId;
        private final String productName;
        private final String primaryImageUrl;
        private final UUID categoryId;
        private final String categoryName;
        private final UUID productVariantId;
        private final String variantName;
        private final String sku;
        private final BigDecimal price;
        private final Long totalQuantityInStock;

        private ProductVariantData(
                UUID productId,
                String productName,
                String primaryImageUrl,
                UUID categoryId,
                String categoryName,
                UUID productVariantId,
                String variantName,
                String sku,
                BigDecimal price,
                Long totalQuantityInStock
        ) {
            this.productId = productId;
            this.productName = productName;
            this.primaryImageUrl = primaryImageUrl;
            this.categoryId = categoryId;
            this.categoryName = categoryName;
            this.productVariantId = productVariantId;
            this.variantName = variantName;
            this.sku = sku;
            this.price = price;
            this.totalQuantityInStock = totalQuantityInStock;
        }
    }

}
