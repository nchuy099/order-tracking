package com.nchuy099.ordertracking.controller;

import com.nchuy099.ordertracking.dto.request.CreateUserRequest;
import com.nchuy099.ordertracking.entity.UserEntity;
import com.nchuy099.ordertracking.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public String hello() {
        return "hello";
    }

    @PostMapping
    public ResponseEntity<UserEntity> create(@RequestBody CreateUserRequest request) {
        UserEntity user = userService.create(request);
        return ResponseEntity.ok(user);
    }
}
