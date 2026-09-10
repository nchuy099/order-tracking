package com.nchuy099.ordertracking.repository;

import com.nchuy099.ordertracking.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, UUID> {

    Optional<PaymentEntity> findTopByOrderIdOrderByCreatedAtDesc(UUID orderId);
}
