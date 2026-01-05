package com.example.OrderService.service;

import com.example.OrderService.client.UserClient;
import com.example.OrderService.dto.*;
import com.example.OrderService.entity.Item;
import com.example.OrderService.entity.Order;
import com.example.OrderService.entity.OrdersItem;
import com.example.OrderService.entity.Status;
import com.example.OrderService.mapper.ItemMapper;
import com.example.OrderService.mapper.OrderMapper;
import com.example.OrderService.repository.ItemRepository;
import com.example.OrderService.repository.OrderRepository;
import com.example.OrderService.spetification.OrderSpecifications;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final ItemRepository itemRepository;
    private final OrderRepository orderRepository;
    private final UserClient userClient;

    @Override
    @Transactional
    public OrderDTO createOrder(OrderRequestDTO dto) {
        log.info("Creating order for user: {}", dto.userId());
        Order order = orderMapper.toEntity(dto);
        order.setOrdersItems(new HashSet<>());
        BigDecimal totalPrice = fillOrderItems(order, dto.items());
        order.setTotalPrice(totalPrice);

        order.setUpdatedAt(LocalDateTime.now());

        Order savedOrder = orderRepository.save(order);
        UserDTO user = fetchUserSafe(savedOrder.getUserId());
        return orderMapper.toDTO(savedOrder, user);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDTO getOrderById(Long id) {
        Order order = findOrderByIdOrThrow(id);
        UserDTO user = fetchUserSafe(order.getUserId());
        return orderMapper.toDTO(order, user);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderDTO> getAllOrders(Pageable pageable, OrderFilterDto filter) {
        Specification<Order> spec = (root, query, cb) -> cb.conjunction();

        if (filter != null) {
            if (filter.getStatus() != null) {
                spec = spec.and(OrderSpecifications.hasStatus(filter.getStatus()));
            }

            if (filter.getCreatedFrom() != null || filter.getCreatedTo() != null) {
                spec = spec.and(OrderSpecifications.createdBetween(
                        filter.getCreatedFrom(),
                        filter.getCreatedTo()
                ));
            }
        }

        Page<Order> orders = orderRepository.findAll(spec, pageable);

        return orders.map(order -> {
            UserDTO user = fetchUserSafe(order.getUserId());
            return orderMapper.toDTO(order, user);
        });
    }

    @Override
    @Transactional
    public OrderDTO updateOrder(Long id, OrderRequestDTO dto) {
        log.info("Updating order id: {}", id);
        Order updatedOrder = findOrderByIdOrThrow(id);

        if (dto.userId() != null) {
            updatedOrder.setUserId(dto.userId());
        }

        if (dto.items() != null && !dto.items().isEmpty()) {
            updatedOrder.getOrdersItems().clear();
            BigDecimal newTotal = fillOrderItems(updatedOrder, dto.items());
            updatedOrder.setTotalPrice(newTotal);
            updatedOrder.setStatus(Status.PENDING);
        }

        updatedOrder.setUpdatedAt(LocalDateTime.now());

        Order savedOrder = orderRepository.save(updatedOrder);
        UserDTO user = fetchUserSafe(savedOrder.getUserId());
        return orderMapper.toDTO(savedOrder, user);
    }

    @Override
    @Transactional
    public void setStatus(Long id, Status status) {
        Order order = findOrderByIdOrThrow(id);

        if (order.getStatus() != status) {
            order.setStatus(status);
            order.setUpdatedAt(LocalDateTime.now());
            orderRepository.save(order);
        }
    }

    @Override
    @Transactional
    public void deleteOrder(Long id) {
        Order order = findOrderByIdOrThrow(id);

        order.setDeleted(true);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
    }

    private BigDecimal fillOrderItems(Order order, Iterable<OrderItemDTO> itemsDto) {
        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemDTO itemDto : itemsDto) {
            Item item = itemRepository.findById(itemDto.itemId())
                    .orElseThrow(() -> new EntityNotFoundException("Item not found with id: " + itemDto.itemId()));

            OrdersItem ordersItem = new OrdersItem();
            ordersItem.setOrder(order);
            ordersItem.setItem(item);
            ordersItem.setQuantity(itemDto.quantity());

            order.getOrdersItems().add(ordersItem);

            BigDecimal lineTotal = item.getPrice().multiply(BigDecimal.valueOf(itemDto.quantity()));
            total = total.add(lineTotal);
        }
        return total;
    }

    private UserDTO fetchUserSafe(Long userId) {
        return userClient.getUserById(userId);
    }

    private Order findOrderByIdOrThrow(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));
    }
}