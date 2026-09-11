package com.nchuy099.ordertracking.service.spec;

import com.nchuy099.ordertracking.common.ProductVariantStatusEnum;
import com.nchuy099.ordertracking.entity.InventoryEntity;
import com.nchuy099.ordertracking.entity.ProductVariantEntity;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.Locale;

public final class ProductVariantSpecification {

    private ProductVariantSpecification() {
    }

    public static Specification<ProductVariantEntity> isActiveAndNotDeleted() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.and(
                criteriaBuilder.equal(root.get("deleted"), 0),
                criteriaBuilder.equal(root.get("status"), ProductVariantStatusEnum.ACTIVE),
                criteriaBuilder.equal(root.get("product").get("deleted"), 0)
        );
    }

    public static Specification<ProductVariantEntity> hasKeyword(String keyword) {
        return (root, query, criteriaBuilder) -> {
            String keywordPattern = "%" + keyword.toLowerCase(Locale.ROOT) + "%";
            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("product").get("name")), keywordPattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), keywordPattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("sku")), keywordPattern)
            );
        };
    }

    public static Specification<ProductVariantEntity> hasPriceBetween(BigDecimal minPrice, BigDecimal maxPrice) {
        return (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();
            if (minPrice != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice));
            }
            if (maxPrice != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice));
            }
            return predicate;
        };
    }

    public static Specification<ProductVariantEntity> hasTotalStockBetween(
            Integer minQuantityInStock,
            Integer maxQuantityInStock
    ) {
        return (root, query, criteriaBuilder) -> {
            Subquery<Long> stockSubquery = query.subquery(Long.class);
            Root<InventoryEntity> inventory = stockSubquery.from(InventoryEntity.class);
            Expression<Long> totalQuantity = criteriaBuilder.coalesce(
                    criteriaBuilder.sumAsLong(inventory.get("quantityInStock")),
                    0L
            );

            stockSubquery.select(totalQuantity)
                    .where(
                            criteriaBuilder.equal(inventory.get("productVariant"), root),
                            criteriaBuilder.equal(inventory.get("deleted"), 0)
                    );

            if (minQuantityInStock != null && maxQuantityInStock != null) {
                return criteriaBuilder.between(stockSubquery,
                        minQuantityInStock.longValue(), maxQuantityInStock.longValue());
            }
            if (minQuantityInStock != null) {
                return criteriaBuilder.greaterThanOrEqualTo(stockSubquery, minQuantityInStock.longValue());
            }
            if (maxQuantityInStock != null) {
                return criteriaBuilder.lessThanOrEqualTo(stockSubquery, maxQuantityInStock.longValue());
            }
            return criteriaBuilder.conjunction();
        };
    }
}
