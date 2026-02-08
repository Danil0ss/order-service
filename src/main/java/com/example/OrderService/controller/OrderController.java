package com.example.OrderService.controller;

import com.example.OrderService.dto.OrderDTO;
import com.example.OrderService.dto.OrderFilterDto;
import com.example.OrderService.dto.OrderRequestDTO;
import com.example.OrderService.entity.Status;
import com.example.OrderService.service.OrderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;



@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping
     public ResponseEntity<OrderDTO> createOrder(@Valid @RequestBody OrderRequestDTO requestDTO){
        OrderDTO createdOrder =orderService.createOrder(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<OrderDTO>> getAllOrders(Pageable pageable,@ModelAttribute OrderFilterDto filterDto){
        Page<OrderDTO> page = orderService.getAllOrders(pageable,filterDto);
        return ResponseEntity.ok(page);
    }


    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable Long id){
        OrderDTO order =orderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id){
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderDTO> updateOrder(@PathVariable Long id,@Valid @RequestBody OrderRequestDTO requestDTO){
        OrderDTO updatedOrder=orderService.updateOrder(id,requestDTO);
        return ResponseEntity.ok(updatedOrder);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> changeOrderStatus(@PathVariable Long id, @RequestParam Status status){
        orderService.setStatus(id,status);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my")
    public ResponseEntity<Page<OrderDTO>> getMyOrders(
            Pageable pageable,
            @ModelAttribute OrderFilterDto filterDto
    ) {
        Page<OrderDTO> page = orderService.getMyOrders(pageable, filterDto);
        return ResponseEntity.ok(page);
    }

}
