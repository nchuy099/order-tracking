package com.nchuy099.ordertracking.repository;

import com.nchuy099.ordertracking.entity.CartItemEntity;
import com.nchuy099.ordertracking.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartItemRepository extends JpaRepository<CartItemEntity, UUID> {

    Optional<CartItemEntity> findByCartIdAndProductVariantId(UUID cartId, UUID productVariantId);

    @Query("""
        Select ci
        From CartItemEntity ci
        JOIN FETCH ci.productVariant pv
        JOIN FETCH pv.product p
        WHERE ci.cart.id = :cartId
    """)
    List<CartItemEntity> findCartItemsByCartId(UUID cartId);
}
