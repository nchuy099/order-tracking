package com.nchuy099.ordertracking.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "warehouses")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WarehouseEntity extends BaseEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(length = 36)
    private UUID id;

    private String code;

    private String name;

    private String province;

    private String district;

    private String ward;

    private String addressLine;

    @Builder.Default
    private Boolean active = true;

    @OneToMany(mappedBy = "warehouse")
    private List<InventoryEntity> inventories;

    public static void main(String[] args) {
        System.out.println(UUID.randomUUID());
    }
}
