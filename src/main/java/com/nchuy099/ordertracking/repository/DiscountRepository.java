package com.nchuy099.ordertracking.repository;

import com.nchuy099.ordertracking.common.DiscountStatusEnum;
import com.nchuy099.ordertracking.entity.DiscountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DiscountRepository extends JpaRepository<DiscountEntity, UUID> {

    List<DiscountEntity> findByStatus(DiscountStatusEnum status);

    Optional<DiscountEntity> findByCode(String code);
}
