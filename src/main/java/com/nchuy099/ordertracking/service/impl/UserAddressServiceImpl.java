package com.nchuy099.ordertracking.service.impl;

import com.nchuy099.ordertracking.dto.request.CreateUserAddressRequest;
import com.nchuy099.ordertracking.entity.UserAddressEntity;
import com.nchuy099.ordertracking.entity.UserEntity;
import com.nchuy099.ordertracking.exception.BusinessException;
import com.nchuy099.ordertracking.repository.UserAddressRepository;
import com.nchuy099.ordertracking.repository.UserRepository;
import com.nchuy099.ordertracking.service.UserAddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserAddressServiceImpl implements UserAddressService {

    private final UserAddressRepository userAddressRepository;
    private final UserRepository userRepository;

    @Override
    public UserAddressEntity create(CreateUserAddressRequest request) {
        UserEntity user = getCurrentUserEntity();

        UserAddressEntity userAddress = UserAddressEntity.builder()
                .recipientName(request.getRecipientName())
                .recipientPhone(request.getRecipientPhone())
                .province(request.getProvince())
                .district(request.getDistrict())
                .ward(request.getWard())
                .detailAddress(request.getDetailAddress())
                .user(user)
                .build();

        userAddressRepository.save(userAddress);
        return userAddress;
    }

    private UserEntity getCurrentUserEntity() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();
        Optional<UserEntity> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            throw new BusinessException("USER_NOT_FOUND",
                    "User not found",
                    HttpStatus.NOT_FOUND);
        }

        return userOpt.get();
    }
}
