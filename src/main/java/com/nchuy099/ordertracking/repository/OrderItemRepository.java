package com.nchuy099.ordertracking.repository;

import com.nchuy099.ordertracking.entity.OrderItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItemEntity, UUID> {

    @Query("""
            SELECT orderItem
            FROM OrderItemEntity orderItem
            JOIN FETCH orderItem.productVariant productVariant
            JOIN FETCH productVariant.product
            WHERE orderItem.order.id = :orderId
            """)
    List<OrderItemEntity> findAllByOrderIdWithProduct(@Param("orderId") UUID orderId);
}
