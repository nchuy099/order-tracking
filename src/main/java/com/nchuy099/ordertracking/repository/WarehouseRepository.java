package com.nchuy099.ordertracking.repository;

import com.nchuy099.ordertracking.entity.WarehouseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WarehouseRepository extends JpaRepository<WarehouseEntity, UUID> {

    Optional<WarehouseEntity> findByCode(String code);
}
