package com.nchuy099.ordertracking.entity;

import com.nchuy099.ordertracking.common.DiscountStatusEnum;
import com.nchuy099.ordertracking.common.DiscountTypeEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "discounts")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DiscountEntity extends BaseEntity {
    @Id
    @GeneratedValue
    @UuidGenerator
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(length = 36)
    private UUID id;

    private String code;

    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    private DiscountTypeEnum type;

    private BigDecimal value;

    private BigDecimal maxDiscountAmount;

    private Integer usageLimit;

    private Integer usageLimitPerUser;

    private Integer usedCount;

    private LocalDateTime startAt;

    private LocalDateTime endAt;

    @Enumerated(EnumType.STRING)
    private DiscountStatusEnum status;
}
