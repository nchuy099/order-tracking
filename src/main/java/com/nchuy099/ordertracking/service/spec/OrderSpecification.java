package com.nchuy099.ordertracking.service.spec;

import com.nchuy099.ordertracking.common.OrderStatusEnum;
import com.nchuy099.ordertracking.entity.OrderEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.UUID;

public final class OrderSpecification {

    private OrderSpecification() {
    }

    public static Specification<OrderEntity> isNotDeleted() {
        return (root, query, criteriaBuilder) -> {
            Predicate isNotDeleted = criteriaBuilder.equal(root.get("deleted"), 0);
            return isNotDeleted;
        };
    }

    public static Specification<OrderEntity> hasStatusIn(List<OrderStatusEnum> statuses) {
        return (root, query, criteriaBuilder) -> {
            Predicate hasStatus = root.get("status").in(statuses);
            return hasStatus;
        };
    }

    public static Specification<OrderEntity> belongsToCustomer(UUID customerId) {
        return (root, query, criteriaBuilder) -> {
            Predicate belongsToCustomer = criteriaBuilder.equal(
                    root.get("user").get("id"),
                    customerId
            );
            return belongsToCustomer;
        };
    }
}
