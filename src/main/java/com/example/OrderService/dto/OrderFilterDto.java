package com.example.OrderService.dto;

import com.example.OrderService.entity.Status;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderFilterDto {
    private LocalDateTime  createdFrom;

    private LocalDateTime createdTo;

    private Status status;
}
