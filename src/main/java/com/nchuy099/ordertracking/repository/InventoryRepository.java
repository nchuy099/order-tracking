package com.nchuy099.ordertracking.repository;

import com.nchuy099.ordertracking.entity.InventoryEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InventoryRepository extends JpaRepository<InventoryEntity, UUID> {

    @Query("""
        SELECT COALESCE(SUM(i.quantityInStock), 0)
        FROM InventoryEntity i
        WHERE i.productVariant.id = :productVariantId
    """)
    Integer getQuantityInStockByProductVariantId(UUID productVariantId);

    List<InventoryEntity> findAllByProductVariantId(UUID productVariantId);

    @Query("""
        SELECT inventory FROM InventoryEntity inventory
        JOIN FETCH inventory.warehouse
        WHERE inventory.productVariant.id = :productVariantId
        ORDER BY inventory.warehouse.code
    """)
    List<InventoryEntity> findAllByProductVariantIdWithWarehouse(
            @Param("productVariantId") UUID productVariantId
    );

    Optional<InventoryEntity> findByWarehouseIdAndProductVariantId(UUID warehouseId, UUID productVariantId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
              SELECT i
              FROM InventoryEntity i
              WHERE i.productVariant.id = :productVariantId
              ORDER BY i.id

    """)
    List<InventoryEntity> findAllByProductVariantIdForUpdate(
        @Param("productVariantId") UUID productVariantId);
}
