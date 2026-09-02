package com.nchuy099.ordertracking.entity;

import com.nchuy099.ordertracking.common.PaymentMethodEnum;
import com.nchuy099.ordertracking.common.PaymentStatusEnum;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentEntity extends BaseEntity {
    @Id
    @GeneratedValue
    @UuidGenerator
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(length = 36)
    private UUID id;

    private String paymentCode;

    @Enumerated(EnumType.STRING)
    private PaymentMethodEnum method;

    @Enumerated(EnumType.STRING)
    private PaymentStatusEnum status;

    private BigDecimal amount;

    private String providerTransactionId;

    private LocalDateTime paidAt;

    private LocalDateTime expiredAt;

    private String failureReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", referencedColumnName = "id")
    private OrderEntity order;
}
