package com.example.OrderService.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;


import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;


@Entity
@Table(
        name="orders",
        indexes = {
                @Index(name="idx_order_user_id",columnList = "user_id")
        }
)
@SQLDelete(sql = "UPDATE orders SET deleted = true WHERE id = ?")
@SQLRestriction("deleted = false")
@Getter
@Setter
@NoArgsConstructor
public class Order extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false,length = 50)
    private Status status;

    @Column(nullable = false)
    private BigDecimal totalPrice;

    @Column(nullable = false)
    private Boolean deleted=false;

    @OneToMany(mappedBy ="order",orphanRemoval = true,cascade = CascadeType.ALL)
    private Set<OrdersItem> ordersItems=new HashSet<>();


}
