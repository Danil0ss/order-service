package com.example.OrderService.spetification;

import com.example.OrderService.entity.Order;
import com.example.OrderService.entity.Status;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class OrderSpecifications {

    public static Specification<Order> hasStatus(Status status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) return null;
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }

    public static Specification<Order> createdBetween(LocalDateTime start,  LocalDateTime  end) {
        return (root, query, criteriaBuilder) -> {
            if (start == null && end == null) return null;
            if (start != null && end != null) {
                return criteriaBuilder.between(root.get("createdAt"), start, end);
            } else if (start != null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), start);
            } else {
                return criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), end);
            }
        };
    }
}