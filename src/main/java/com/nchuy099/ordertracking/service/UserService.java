package com.nchuy099.ordertracking.service;

import com.nchuy099.ordertracking.dto.request.CreateUserRequest;
import com.nchuy099.ordertracking.entity.UserEntity;

public interface UserService {
    UserEntity create(CreateUserRequest request);

}
