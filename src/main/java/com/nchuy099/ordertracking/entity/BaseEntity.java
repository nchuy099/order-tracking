package com.nchuy099.ordertracking.entity;

import jakarta.persistence.Entity;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;

public class BaseEntity implements Serializable {

    private LocalDateTime createdAt;

    private String createdBy;

    private LocalDateTime updatedAt;

    private String updatedBy;

    private Boolean deleted = false;
}
