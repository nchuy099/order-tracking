package com.nchuy099.ordertracking.repository;

import com.nchuy099.ordertracking.entity.CategoryEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<CategoryEntity, UUID> {

    List<CategoryEntity> findByActiveTrueAndDeleted(Integer deleted, Pageable pageable);
}
