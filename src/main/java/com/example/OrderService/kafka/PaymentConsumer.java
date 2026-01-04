package com.example.OrderService.kafka;

import com.example.OrderService.dto.PaymentEvent;
import com.example.OrderService.entity.Status;
import com.example.OrderService.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.kafka.annotation.KafkaListener;


@Service
@RequiredArgsConstructor
public class PaymentConsumer {

    private final OrderService orderService;

    @KafkaListener(topics = "CREATE_PAYMENT", groupId = "order-group")
    public void listen(PaymentEvent event) {
        System.out.println("Получено событие платежа для заказа: " + event.getOrderId());

        if ("SUCCESS".equals(event.getStatus())) {
            orderService.setStatus(event.getOrderId(), Status.PAID);
        } else {
            orderService.setStatus(event.getOrderId(), Status.CANCELLED);
        }
    }
}
