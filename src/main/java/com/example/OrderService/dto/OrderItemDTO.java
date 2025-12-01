package com.example.OrderService.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderItemDTO(
        @NotNull(message ="Item ID is required")
        Long itemId,

        @Min(value = 1,message = "Quantity must be at least 1")
        int quantity
) {}
