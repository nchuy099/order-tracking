package com.nchuy099.ordertracking.service.impl;

import com.nchuy099.ordertracking.dto.request.CreateWarehouseRequest;
import com.nchuy099.ordertracking.entity.WarehouseEntity;
import com.nchuy099.ordertracking.exception.BusinessException;
import com.nchuy099.ordertracking.repository.WarehouseRepository;
import com.nchuy099.ordertracking.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseRepository warehouseRepository;

    @Override
    public WarehouseEntity create(CreateWarehouseRequest request) {
        Optional<WarehouseEntity> warehouseOpt = warehouseRepository.findByCode(request.getCode());
        if (warehouseOpt.isPresent()) {
            throw new BusinessException("WAREHOUSE_CODE_ALREADY_EXISTS",
                    "Warehouse code already exists",
                    HttpStatus.BAD_REQUEST);
        }

        WarehouseEntity warehouse = WarehouseEntity.builder()
                .code(request.getCode())
                .name(request.getName())
                .province(request.getProvince())
                .district(request.getDistrict())
                .ward(request.getWard())
                .addressLine(request.getAddressLine())
                .active(request.getActive() == null || request.getActive())
                .build();

        warehouseRepository.save(warehouse);
        return warehouse;
    }
}
