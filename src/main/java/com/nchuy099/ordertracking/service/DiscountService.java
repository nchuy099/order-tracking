package com.nchuy099.ordertracking.service;

import com.nchuy099.ordertracking.dto.request.CreateDiscountRequest;
import com.nchuy099.ordertracking.entity.DiscountEntity;

import java.util.List;

public interface DiscountService {

    List<DiscountEntity> getAll();

    DiscountEntity create(CreateDiscountRequest request);
}
