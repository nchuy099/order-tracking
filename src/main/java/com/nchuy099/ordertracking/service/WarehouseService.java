package com.nchuy099.ordertracking.service;

import com.nchuy099.ordertracking.dto.request.CreateWarehouseRequest;
import com.nchuy099.ordertracking.entity.WarehouseEntity;

public interface WarehouseService {

    WarehouseEntity create(CreateWarehouseRequest request);
}
