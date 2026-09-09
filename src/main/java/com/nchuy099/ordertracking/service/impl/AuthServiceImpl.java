package com.nchuy099.ordertracking.service.impl;

import com.nchuy099.ordertracking.common.RoleEnum;
import com.nchuy099.ordertracking.common.UserStatusEnum;
import com.nchuy099.ordertracking.dto.request.LoginRequest;
import com.nchuy099.ordertracking.dto.request.RegisterRequest;
import com.nchuy099.ordertracking.dto.response.AuthResponse;
import com.nchuy099.ordertracking.entity.UserEntity;
import com.nchuy099.ordertracking.exception.BusinessException;
import com.nchuy099.ordertracking.repository.UserRepository;
import com.nchuy099.ordertracking.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);
        String phoneNumber = request.getPhoneNumber().trim();

        if (userRepository.findByEmail(email).isPresent()) {
            throw new BusinessException(
                    "EMAIL_ALREADY_EXISTS",
                    "Email is already registered",
                    HttpStatus.CONFLICT
            );
        }

        if (userRepository.findByPhoneNumber(phoneNumber).isPresent()) {
            throw new BusinessException(
                    "PHONE_NUMBER_ALREADY_EXISTS",
                    "Phone number is already registered",
                    HttpStatus.CONFLICT
            );
        }

        UserEntity user = new UserEntity();
        user.setFullName(request.getFullName().trim());
        user.setDateOfBirth(request.getDateOfBirth());
        user.setEmail(email);
        user.setPhoneNumber(phoneNumber);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(RoleEnum.CUSTOMER);
        user.setStatus(UserStatusEnum.ACTIVE);

        UserEntity savedUser = userRepository.save(user);
        return AuthResponse.builder()
                .userId(savedUser.getId())
                .fullName(savedUser.getFullName())
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String emailOrPhone = request.getEmailOrPhone().trim();
        UserEntity user = userRepository.findByEmailOrPhoneNumber(
                        emailOrPhone.toLowerCase(Locale.ROOT), emailOrPhone
                )
                .orElseThrow(() -> new BusinessException(
                        "INVALID_CREDENTIALS",
                        "Email, phone number, or password is incorrect",
                        HttpStatus.UNAUTHORIZED
                ));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException("INVALID_CREDENTIALS",
                    "Email, phone number, or password is incorrect",
                    HttpStatus.UNAUTHORIZED
            );
        }

        if (user.getStatus() != UserStatusEnum.ACTIVE) {
            throw new BusinessException(
                    "USER_NOT_ACTIVE",
                    "User account not active",
                    HttpStatus.FORBIDDEN
            );
        }

        if (user.getRole() != request.getRole()) {
            throw new BusinessException(
                    "ROLE_NOT_ALLOWED",
                    "The selected role not match this account",
                    HttpStatus.FORBIDDEN
            );
        }

        return AuthResponse.builder()
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}
