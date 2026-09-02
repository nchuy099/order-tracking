package com.nchuy099.ordertracking.repository;

import com.nchuy099.ordertracking.entity.OrderItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrderItemRepository extends JpaRepository<OrderItemEntity, UUID> {
}
