package com.example.OrderService.dto;

import com.example.OrderService.entity.Status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


public record OrderDTO(
        Long id,
        UserDTO user,
        Status status,
        BigDecimal totalPrice,
        Boolean deleted,
        LocalDateTime createdAt,
        LocalDateTime  updatedAt,
        List<OrderItemResponseDTO> items
){}

