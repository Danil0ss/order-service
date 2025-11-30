
package com.example.OrderService.repository;

import com.example.OrderService.entity.Order;
import com.example.OrderService.entity.Status;
import org.hibernate.annotations.CurrentTimestamp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order,Long> , JpaSpecificationExecutor<Order> {
    List<Order> findByUserId(Long userId);

    @Modifying
    @Query("UPDATE Order o SET o.status=COALESCE(:status,o.status)," +
            "o.totalPrice=COALESCE(:totalPrice,o.totalPrice)," +
            "o.deleted=COALESCE(:deleted,o.deleted)," +
            "o.updatedAt=CURRENT_TIMESTAMP " +
            "WHERE o.id=:id")
    int updateOrder(@Param("id")Long id,
                    @Param("status")Status status,
                    @Param("totalPrice")BigDecimal totalPrice,
                    @Param("deleted")Boolean deleted);
}

