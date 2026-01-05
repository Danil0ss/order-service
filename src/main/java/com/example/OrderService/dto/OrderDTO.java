package com.example.OrderService.dto;

import com.example.OrderService.entity.Status;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;


public record OrderDTO(
        Long id,
        UserDTO user,
        Status status,
        BigDecimal totalPrice,
        Boolean deleted,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        List<OrderItemResponseDTO> items
){}

