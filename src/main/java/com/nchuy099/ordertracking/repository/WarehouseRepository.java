package com.nchuy099.ordertracking.repository;

import com.nchuy099.ordertracking.entity.WarehouseEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WarehouseRepository extends JpaRepository<WarehouseEntity, UUID> {

    Optional<WarehouseEntity> findByCode(String code);
}
