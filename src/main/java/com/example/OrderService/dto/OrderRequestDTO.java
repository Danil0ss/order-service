package com.example.OrderService.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record OrderRequestDTO (
        @NotBlank(message = "User ID required")
        Long userId,

        @NotEmpty(message = "Order must not be empty")
        @Valid
        List<OrderItemDTO> items
){}
