package com.nchuy099.ordertracking.repository;

import com.nchuy099.ordertracking.entity.ProductEntity;
import com.nchuy099.ordertracking.entity.ProductVariantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.List;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariantEntity, UUID> {

    List<ProductVariantEntity> findAllByProductId(UUID productId);
}
