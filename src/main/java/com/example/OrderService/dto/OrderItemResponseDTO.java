package com.example.OrderService.dto;

import java.math.BigDecimal;

public record OrderItemResponseDTO(
        String name,
        BigDecimal price,
        int quantity
) {}
