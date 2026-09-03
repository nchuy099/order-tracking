package com.nchuy099.ordertracking.service.impl;

import com.nchuy099.ordertracking.common.GenderEnum;
import com.nchuy099.ordertracking.common.RoleEnum;
import com.nchuy099.ordertracking.common.UserStatusEnum;
import com.nchuy099.ordertracking.dto.request.CreateUserRequest;
import com.nchuy099.ordertracking.entity.UserEntity;
import com.nchuy099.ordertracking.repository.UserRepository;
import com.nchuy099.ordertracking.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserEntity create(CreateUserRequest request) {
        Optional<UserEntity> optionalUser = userRepository.findByEmail(request.getEmail());

        if (optionalUser.isPresent()) {
            throw new RuntimeException("Email exists");
        }

        UserEntity userEntity = new UserEntity();
        userEntity.setEmail(request.getEmail());
        userEntity.setDateOfBirth(request.getDateOfBirth());
        userEntity.setPhoneNumber(request.getPhoneNumber());
        userEntity.setFullName(request.getFullName());
        userEntity.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        userEntity.setRole(RoleEnum.valueOf(request.getRole()));

        if (request.getGender() != null) {
            userEntity.setGender(GenderEnum.valueOf(request.getGender()));
        }

        if (request.getStatus() != null) {
            userEntity.setStatus(UserStatusEnum.valueOf(request.getStatus()));
        }

        if (request.getAvatarUrl() != null) {
            userEntity.setAvatarUrl(request.getAvatarUrl());
        }

        userRepository.save(userEntity);
        return userEntity;
    }
}
