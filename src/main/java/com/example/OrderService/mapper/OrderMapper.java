package com.example.OrderService.mapper;

import com.example.OrderService.dto.OrderDTO;
import com.example.OrderService.dto.OrderRequestDTO;
import com.example.OrderService.dto.UserDTO;
import com.example.OrderService.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {OrderItemMapper.class})
public interface OrderMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", constant = "CREATED")
    @Mapping(target = "totalPrice", constant = "0")
    @Mapping(target = "deleted", constant = "false")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "ordersItems", ignore = true)
    @Mapping(source = "userId", target = "userId")
    Order toEntity(OrderRequestDTO request);

    @Mapping(source = "user", target = "user")
    @Mapping(source = "order.id", target = "id")
    @Mapping(source = "order.status", target = "status")
    @Mapping(source = "order.totalPrice", target = "totalPrice")
    @Mapping(source = "order.deleted", target = "deleted")
    @Mapping(source = "order.createdAt", target = "createdAt")
    @Mapping(source = "order.updatedAt", target = "updatedAt")
    @Mapping(source = "order.ordersItems", target = "items")
    OrderDTO toDTO(Order order, UserDTO user);
}