package com.nchuy099.ordertracking.service.impl;

import com.nchuy099.ordertracking.common.DiscountStatusEnum;
import com.nchuy099.ordertracking.common.DiscountTypeEnum;
import com.nchuy099.ordertracking.dto.request.CreateDiscountRequest;
import com.nchuy099.ordertracking.entity.DiscountEntity;
import com.nchuy099.ordertracking.exception.BusinessException;
import com.nchuy099.ordertracking.repository.DiscountRepository;
import com.nchuy099.ordertracking.service.DiscountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DiscountServiceImpl implements DiscountService {

    private final DiscountRepository discountRepository;

    @Override
    public List<DiscountEntity> getAll() {
        return discountRepository.findByStatus(DiscountStatusEnum.ACTIVE);
    }

    @Override
    public DiscountEntity create(CreateDiscountRequest request) {
        Optional<DiscountEntity> discountOpt = discountRepository.findByCode(request.getCode());
        if (discountOpt.isPresent()) {
            throw new BusinessException("DISCOUNT_CODE_ALREADY_EXISTS",
                    "Discount code already exists",
                    HttpStatus.BAD_REQUEST);
        }

        DiscountEntity discount = new DiscountEntity();
        discount.setCode(request.getCode());
        discount.setName(request.getName());
        discount.setDescription(request.getDescription());
        discount.setType(DiscountTypeEnum.valueOf(request.getType()));
        discount.setValue(request.getValue());
        discount.setMaxDiscountAmount(request.getMaxDiscountAmount());
        discount.setUsageLimit(request.getUsageLimit());
        discount.setUsageLimitPerUser(request.getUsageLimitPerUser());
        discount.setUsedCount(0);
        discount.setStartAt(request.getStartAt());
        discount.setEndAt(request.getEndAt());
        discount.setStatus(request.getStatus() == null
                ? DiscountStatusEnum.ACTIVE
                : DiscountStatusEnum.valueOf(request.getStatus()));

        discountRepository.save(discount);
        return discount;
    }
}
