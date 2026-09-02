package com.nchuy099.ordertracking.service.impl;

import com.nchuy099.ordertracking.common.DiscountStatusEnum;
import com.nchuy099.ordertracking.entity.DiscountEntity;
import com.nchuy099.ordertracking.repository.DiscountRepository;
import com.nchuy099.ordertracking.service.DiscountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DiscountServiceImpl implements DiscountService {

    private final DiscountRepository discountRepository;

    @Override
    public List<DiscountEntity> getAll() {
        return discountRepository.findByStatus(DiscountStatusEnum.ACTIVE);
    }
}
