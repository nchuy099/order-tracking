package com.nchuy099.ordertracking.repository;

import com.nchuy099.ordertracking.entity.OrderEntity;
import com.nchuy099.ordertracking.common.OrderStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, UUID> {

    @Query("""
            SELECT
                COUNT(orderEntity) AS totalOrdersToday,
                COALESCE(SUM(CASE WHEN orderEntity.status = :deliveredStatus THEN 1 ELSE 0 END), 0) AS deliveredOrdersToday,
                COALESCE(SUM(CASE WHEN orderEntity.status = :pendingStatus THEN 1 ELSE 0 END), 0) AS pendingOrdersToday
            FROM OrderEntity orderEntity
            WHERE orderEntity.deleted = 0
              AND orderEntity.orderedAt >= :startOfDay
              AND orderEntity.orderedAt < :startOfNextDay
            """)
    DailyOrderSummaryProjection getDailySummary(
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("startOfNextDay") LocalDateTime startOfNextDay,
            @Param("deliveredStatus") OrderStatusEnum deliveredStatus,
            @Param("pendingStatus") OrderStatusEnum pendingStatus
    );
}
