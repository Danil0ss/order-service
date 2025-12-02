package com.example.OrderService.service;

import com.example.OrderService.dto.OrderDTO;
import com.example.OrderService.dto.OrderFilterDto;
import com.example.OrderService.dto.OrderRequestDTO;
import com.example.OrderService.entity.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {
    OrderDTO createOrder(OrderRequestDTO dto);
    OrderDTO getOrderById(Long id);
    Page<OrderDTO> getAllOrders(Pageable pageable, OrderFilterDto filter);
    OrderDTO updateOrder(Long id,OrderRequestDTO dto);
    void setStatus(Long id, Status status);
    void deleteOrder(Long id);
}
