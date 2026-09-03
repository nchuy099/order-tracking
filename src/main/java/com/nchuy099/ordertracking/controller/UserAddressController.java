package com.nchuy099.ordertracking.controller;

import com.nchuy099.ordertracking.dto.request.CreateUserAddressRequest;
import com.nchuy099.ordertracking.entity.UserAddressEntity;
import com.nchuy099.ordertracking.service.UserAddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user-addresses")
@RequiredArgsConstructor
public class UserAddressController {

    private final UserAddressService userAddressService;

    @PostMapping
    public ResponseEntity<UserAddressEntity> create(@RequestBody CreateUserAddressRequest request) {
        UserAddressEntity userAddress = userAddressService.create(request);
        return ResponseEntity.ok(userAddress);
    }
}
