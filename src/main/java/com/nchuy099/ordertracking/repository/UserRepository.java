package com.nchuy099.ordertracking.repository;

import com.nchuy099.ordertracking.dto.request.CreateUserRequest;
import com.nchuy099.ordertracking.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByEmail(String email);
}
