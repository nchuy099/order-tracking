package com.nchuy099.ordertracking.dto.response;

import com.nchuy099.ordertracking.common.RoleEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private UUID userId;
    private String fullName;
    private String email;
    private RoleEnum role;
}
