package com.nchuy099.ordertracking.repository;

import com.nchuy099.ordertracking.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.Optional;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, UUID> {

    Optional<PaymentEntity> findTopByOrderIdOrderByCreatedAtDesc(UUID orderId);

    @Query("""
            SELECT payment
            FROM PaymentEntity payment
            WHERE payment.order.id IN :orderIds
            ORDER BY payment.order.id, payment.createdAt DESC
            """)
    List<PaymentEntity> findAllByOrderIdInOrderByCreatedAtDesc(
            @Param("orderIds") List<UUID> orderIds
    );
}
