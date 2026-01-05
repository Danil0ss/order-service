package com.example.OrderService.service;

import com.example.OrderService.client.UserClient;
import com.example.OrderService.dto.*;
import com.example.OrderService.entity.Item;
import com.example.OrderService.entity.Order;
import com.example.OrderService.entity.Status;
import com.example.OrderService.mapper.OrderMapper;
import com.example.OrderService.repository.ItemRepository;
import com.example.OrderService.repository.OrderRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock private OrderRepository orderRepository;
    @Mock private ItemRepository itemRepository;
    @Mock private OrderMapper orderMapper;
    @Mock private UserClient userClient;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    void createOrder_ShouldCalculatePriceAndReturnDTO() {
        Long userId = 1L;
        Long itemId = 10L;
        OrderRequestDTO request = new OrderRequestDTO(userId, List.of(new OrderItemDTO(itemId, 2)));

        Item item = new Item();
        item.setId(itemId);
        item.setPrice(BigDecimal.valueOf(100));

        Order order = new Order();
        order.setOrdersItems(new HashSet<>());

        Order savedOrder = new Order();
        savedOrder.setId(55L);
        savedOrder.setUserId(userId);
        savedOrder.setTotalPrice(BigDecimal.valueOf(200));

        UserDTO userDto = new UserDTO(userId, "test@test.com", "John", "Doe");

        when(orderMapper.toEntity(request)).thenReturn(order);
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(orderRepository.save(order)).thenReturn(savedOrder);
        when(userClient.getUserById(userId)).thenReturn(userDto);
        when(orderMapper.toDTO(savedOrder, userDto)).thenReturn(
                new OrderDTO(55L, userDto, Status.CREATED, BigDecimal.valueOf(200), false, null, null, List.of())
        );

        OrderDTO result = orderService.createOrder(request);

        assertThat(result.totalPrice()).isEqualTo(BigDecimal.valueOf(200));
        verify(orderRepository).save(order);
    }

    @Test
    void getOrderById_ShouldReturnOrder_WhenExists() {
        Long id = 1L;
        Order order = new Order();
        order.setId(id);
        order.setUserId(10L);

        when(orderRepository.findById(id)).thenReturn(Optional.of(order));
        when(userClient.getUserById(10L)).thenReturn(new UserDTO(10L, "e", "n", "s"));
        when(orderMapper.toDTO(any(), any())).thenReturn(mock(OrderDTO.class));

        orderService.getOrderById(id);

        verify(orderRepository).findById(id);
    }

    @Test
    void getOrderById_ShouldThrow_WhenNotFound() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> orderService.getOrderById(1L));
    }

    @Test
    void updateOrder_ShouldUpdateFieldsAndSave() {
        Long orderId = 1L;
        Long itemId = 2L;
        OrderRequestDTO request = new OrderRequestDTO(5L, List.of(new OrderItemDTO(itemId, 1)));

        Order existingOrder = new Order();
        existingOrder.setId(orderId);
        existingOrder.setUserId(1L);
        existingOrder.setOrdersItems(new HashSet<>());

        Item item = new Item();
        item.setId(itemId);
        item.setPrice(BigDecimal.TEN);

        UserDTO userDto = new UserDTO(5L, "update@test.com", "Upd", "User");

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(existingOrder));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(orderRepository.save(existingOrder)).thenReturn(existingOrder);
        when(userClient.getUserById(5L)).thenReturn(userDto);
        when(orderMapper.toDTO(any(), any())).thenReturn(mock(OrderDTO.class));

        orderService.updateOrder(orderId, request);

        assertThat(existingOrder.getUserId()).isEqualTo(5L);
        assertThat(existingOrder.getStatus()).isEqualTo(Status.PENDING);
        verify(orderRepository).save(existingOrder);
    }

    @Test
    void deleteOrder_ShouldPerformSoftDelete() {
        Long orderId = 1L;
        Order order = new Order();
        order.setId(orderId);
        order.setDeleted(false);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        orderService.deleteOrder(orderId);

        assertThat(order.getDeleted()).isTrue();
        verify(orderRepository).save(order);

        verify(orderRepository, never()).delete(any(Order.class));
    }

    @Test
    void setStatus_ShouldUpdateStatus() {
        Long orderId = 1L;
        Order order = new Order();
        order.setId(orderId);
        order.setStatus(Status.CREATED);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        orderService.setStatus(orderId, Status.PROCESSING);

        assertThat(order.getStatus()).isEqualTo(Status.PROCESSING);
        verify(orderRepository).save(order);
    }

    @Test
    void getAllOrders_ShouldReturnPage() {
        Pageable pageable = Pageable.unpaged();

        OrderFilterDto filter = new OrderFilterDto();
        filter.setStatus(Status.CREATED);

        Order order = new Order();
        order.setUserId(1L);
        Page<Order> page = new PageImpl<>(List.of(order));

        when(orderRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(userClient.getUserById(1L)).thenReturn(new UserDTO(1L, "t", "t", "t"));
        when(orderMapper.toDTO(any(), any())).thenReturn(mock(OrderDTO.class));

        Page<OrderDTO> result = orderService.getAllOrders(pageable, filter);

        assertThat(result.getContent()).hasSize(1);
        verify(orderRepository).findAll(any(Specification.class), eq(pageable));
    }
}