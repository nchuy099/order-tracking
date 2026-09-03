package com.nchuy099.ordertracking.repository;

import com.nchuy099.ordertracking.entity.UserAddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserAddressRepository extends JpaRepository<UserAddressEntity, UUID> {

    Optional<UserAddressEntity> findByIdAndUserId(UUID id, UUID userId);
}
