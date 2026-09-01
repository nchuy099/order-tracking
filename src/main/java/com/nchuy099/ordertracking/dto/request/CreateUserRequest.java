package com.nchuy099.ordertracking.dto.request;


import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CreateUserRequest {

    private String fullName;
    private LocalDate dateOfBirth;
    private String gender;
    private String phoneNumber;
    private String email;
    private String password;
    private String role;
    private String status;
    private String avatarUrl;

}
