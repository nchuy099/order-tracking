package com.nchuy099.ordertracking.entity;

import com.nchuy099.ordertracking.common.OrderStatusEnum;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderEntity extends BaseEntity {
    @Id
    @GeneratedValue
    @UuidGenerator
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(length = 36)
    private UUID id;

    private String code;

    @Enumerated(EnumType.STRING)
    private OrderStatusEnum status;

    private String recipientName;

    @Column(name = "recipient_phone_number")
    private String recipientPhone;

    private String shippingAddress;

    @Column(name = "subtotal")
    private BigDecimal subTotal;

    private BigDecimal discountAmount;

    private BigDecimal shippingFee;

    private BigDecimal grandTotal;

    private String note;

    private LocalDateTime orderedAt;

    private LocalDateTime cancelledAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private UserEntity user;
}
