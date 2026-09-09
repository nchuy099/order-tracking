package com.nchuy099.ordertracking.service;

import com.nchuy099.ordertracking.dto.request.LoginRequest;
import com.nchuy099.ordertracking.dto.request.RegisterRequest;
import com.nchuy099.ordertracking.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}
