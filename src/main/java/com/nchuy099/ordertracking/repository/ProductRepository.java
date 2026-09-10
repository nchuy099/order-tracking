package com.nchuy099.ordertracking.repository;

import com.nchuy099.ordertracking.entity.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, UUID> {

    @Query(value = """
            SELECT
                COALESCE(SUM(COALESCE(stock.total_quantity, 0) * variant.price), 0) AS totalInventoryValue,
                COALESCE(SUM(variant.price), 0) AS totalProductValue,
                COALESCE(SUM(CASE
                    WHEN COALESCE(stock.total_quantity, 0) BETWEEN 1 AND :limitedStock - 1 THEN 1
                    ELSE 0
                END), 0) AS lowStockVariantCount,
                COALESCE(SUM(CASE
                    WHEN COALESCE(stock.total_quantity, 0) = 0 THEN 1
                    ELSE 0
                END), 0) AS outOfStockVariantCount
            FROM product_variants variant
            JOIN products product ON product.id = variant.product_id
            LEFT JOIN (
                SELECT product_variant_id, SUM(quantity_in_stock) AS total_quantity
                FROM inventories
                WHERE deleted = 0
                GROUP BY product_variant_id
            ) stock ON stock.product_variant_id = variant.id
            WHERE variant.deleted = 0
              AND product.deleted = 0
            """, nativeQuery = true)
    ProductSummaryProjection getSummary(@Param("limitedStock") int limitedStock);

    @Query(
            value = """
                    SELECT product
                    FROM ProductEntity product
                    LEFT JOIN FETCH product.category
                    WHERE product.deleted = :deleted
                    """,
            countQuery = """
                    SELECT COUNT(product)
                    FROM ProductEntity product
                    WHERE product.deleted = :deleted
                    """
    )
    Page<ProductEntity> findByDeleted(@Param("deleted") Integer deleted, Pageable pageable);

    @Query("""
        SELECT product FROM ProductEntity product
        LEFT JOIN FETCH product.category
        WHERE product.id = :productId
    """)
    Optional<ProductEntity> findByIdWithCategory(@Param("productId") UUID productId);
}
