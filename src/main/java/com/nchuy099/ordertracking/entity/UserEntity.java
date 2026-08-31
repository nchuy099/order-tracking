package com.nchuy099.ordertracking.entity;

import com.nchuy099.ordertracking.common.GenderEnum;
import com.nchuy099.ordertracking.common.RoleEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.UUID;

@Table(name = "users")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity extends BaseEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(length = 36)
    private UUID id;

    private String fullName;

    private LocalDate dateOfBirth;

    private GenderEnum gender;

    @Column(unique = true)
    private String email;

    private String passwordHash;

    @Column(nullable = false)
    private RoleEnum role;

    private String avatarUrl;
}
