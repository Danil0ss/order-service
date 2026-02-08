package com.example.OrderService.dto;

import com.example.OrderService.entity.Status;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class OrderFilterDto {
    private OffsetDateTime createdFrom;

    private OffsetDateTime createdTo;

    private Status status;
}
