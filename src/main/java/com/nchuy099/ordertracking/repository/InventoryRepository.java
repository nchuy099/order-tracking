package com.nchuy099.ordertracking.repository;

import com.nchuy099.ordertracking.entity.InventoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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

    Optional<InventoryEntity> findByWarehouseIdAndProductVariantId(UUID warehouseId, UUID productVariantId);
}
