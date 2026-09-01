package com.nchuy099.ordertracking.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nchuy099.ordertracking.common.ProductVariantStatusEnum;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Table(name = "product_variants")
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariantEntity extends BaseEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(length = 36)
    private UUID id;

    private String sku;

    private String name;

    private BigDecimal price;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private ProductVariantStatusEnum status = ProductVariantStatusEnum.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", referencedColumnName = "id")
    private ProductEntity product;

    @OneToMany(mappedBy = "productVariant")
    private List<InventoryEntity> inventories;
}
