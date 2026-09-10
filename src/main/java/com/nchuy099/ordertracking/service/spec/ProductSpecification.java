package com.nchuy099.ordertracking.service.spec;

import com.nchuy099.ordertracking.common.ProductVariantStatusEnum;
import com.nchuy099.ordertracking.entity.InventoryEntity;
import com.nchuy099.ordertracking.entity.ProductEntity;
import com.nchuy099.ordertracking.entity.ProductVariantEntity;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ProductSpecification {

    private ProductSpecification() {
    }

    public static Specification<ProductEntity> isNotDeleted() {
        return (root, query, criteriaBuilder) -> {
            Predicate isNotDeleted = criteriaBuilder.equal(root.get("deleted"), 0);
            return isNotDeleted;
        };
    }

    public static Specification<ProductEntity> hasNameContaining(String keyword) {
        return (root, query, criteriaBuilder) -> {
            String keywordPattern = "%" + keyword.toLowerCase(Locale.ROOT) + "%";
            Predicate hasNameContaining = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("name")),
                    keywordPattern
            );
            return hasNameContaining;
        };
    }

    public static Specification<ProductEntity> hasVariantPriceBetween(BigDecimal minPrice, BigDecimal maxPrice) {
        return (root, query, criteriaBuilder) -> {
            Subquery<Integer> variantSubquery = query.subquery(Integer.class);
            Root<ProductVariantEntity> variant = variantSubquery.from(ProductVariantEntity.class);
            List<Predicate> conditions = getActiveVariantConditions(root, variant, criteriaBuilder);

            if (minPrice != null) {
                Predicate priceIsAtLeastMinimum = criteriaBuilder.greaterThanOrEqualTo(
                        variant.get("price"),
                        minPrice
                );
                conditions.add(priceIsAtLeastMinimum);
            }
            if (maxPrice != null) {
                Predicate priceIsAtMostMaximum = criteriaBuilder.lessThanOrEqualTo(
                        variant.get("price"),
                        maxPrice
                );
                conditions.add(priceIsAtMostMaximum);
            }

            variantSubquery.select(criteriaBuilder.literal(1))
                    .where(conditions.toArray(Predicate[]::new));

            Predicate hasVariantInPriceRange = criteriaBuilder.exists(variantSubquery);
            return hasVariantInPriceRange;
        };
    }

    public static Specification<ProductEntity> hasVariantStockBetween(
            Integer minQuantityInStock,
            Integer maxQuantityInStock
    ) {
        return (root, query, criteriaBuilder) -> {
            Subquery<Integer> inventorySubquery = query.subquery(Integer.class);
            Root<ProductVariantEntity> variant = inventorySubquery.from(ProductVariantEntity.class);
            Join<ProductVariantEntity, InventoryEntity> inventory = variant.join("inventories", JoinType.LEFT);
            inventory.on(criteriaBuilder.equal(inventory.get("deleted"), 0));

            Expression<Integer> quantityInStock = criteriaBuilder.coalesce(inventory.get("quantityInStock"), 0);
            Expression<Long> totalQuantityInStock = criteriaBuilder.sumAsLong(quantityInStock);
            List<Predicate> conditions = getActiveVariantConditions(root, variant, criteriaBuilder);

            inventorySubquery.select(criteriaBuilder.literal(1))
                    .where(conditions.toArray(Predicate[]::new))
                    .groupBy(variant.get("id"));

            if (minQuantityInStock != null && maxQuantityInStock != null) {
                Predicate stockIsInRange = criteriaBuilder.between(
                        totalQuantityInStock,
                        minQuantityInStock.longValue(),
                        maxQuantityInStock.longValue()
                );
                inventorySubquery.having(stockIsInRange);
            } else if (minQuantityInStock != null) {
                Predicate stockIsAtLeastMinimum = criteriaBuilder.greaterThanOrEqualTo(
                        totalQuantityInStock,
                        minQuantityInStock.longValue()
                );
                inventorySubquery.having(stockIsAtLeastMinimum);
            } else if (maxQuantityInStock != null) {
                Predicate stockIsAtMostMaximum = criteriaBuilder.lessThanOrEqualTo(
                        totalQuantityInStock,
                        maxQuantityInStock.longValue()
                );
                inventorySubquery.having(stockIsAtMostMaximum);
            }

            Predicate hasVariantWithMatchingStock = criteriaBuilder.exists(inventorySubquery);
            return hasVariantWithMatchingStock;
        };
    }

    private static List<Predicate> getActiveVariantConditions(
            Root<ProductEntity> product,
            Root<ProductVariantEntity> variant,
            CriteriaBuilder criteriaBuilder
    ) {
        List<Predicate> conditions = new ArrayList<>();
        conditions.add(criteriaBuilder.equal(variant.get("product"), product));
        conditions.add(criteriaBuilder.equal(variant.get("deleted"), 0));
        conditions.add(criteriaBuilder.equal(variant.get("status"), ProductVariantStatusEnum.ACTIVE));
        return conditions;
    }
}
