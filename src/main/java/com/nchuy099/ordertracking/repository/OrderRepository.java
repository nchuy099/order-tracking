package com.nchuy099.ordertracking.repository;

import com.nchuy099.ordertracking.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrderRepository extends JpaRepository<OrderEntity, UUID> {
}
