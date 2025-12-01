package com.example.OrderService.dto;

import java.math.BigDecimal;

public record ItemResponseDTO(
        Long id,
        String name,
        BigDecimal price

) {}
