package com.nchuy099.ordertracking.service;

import com.nchuy099.ordertracking.dto.request.CreateUserAddressRequest;
import com.nchuy099.ordertracking.entity.UserAddressEntity;

public interface UserAddressService {

    UserAddressEntity create(CreateUserAddressRequest request);
}
