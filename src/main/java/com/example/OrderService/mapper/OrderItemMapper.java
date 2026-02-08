package com.example.OrderService.mapper;

import com.example.OrderService.dto.OrderItemResponseDTO;
import com.example.OrderService.entity.OrdersItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {

    @Mapping(source = "item.name", target = "name")
    @Mapping(source = "item.price", target = "price")
    @Mapping(source = "quantity", target = "quantity")
    OrderItemResponseDTO toItemDTO(OrdersItem ordersItem);
}
