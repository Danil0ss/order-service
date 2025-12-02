package com.example.OrderService.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record OrderRequestDTO (
        @NotNull(message = "User ID is required")
        Long userId,

        @NotEmpty(message = "Order must not be empty")
        @Valid
        List<OrderItemDTO> items
){}
