package com.nchuy099.ordertracking.repository;

import com.nchuy099.ordertracking.dto.response.CartResponse;
import com.nchuy099.ordertracking.entity.CartEntity;
import com.nchuy099.ordertracking.entity.CartItemEntity;
import com.nchuy099.ordertracking.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartRepository extends JpaRepository<CartEntity, UUID> {
    Optional<CartEntity> findByUserId(UUID userId);



}
