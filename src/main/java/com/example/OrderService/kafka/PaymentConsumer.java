package com.example.OrderService.kafka;

import com.example.OrderService.dto.PaymentEvent;
import com.example.OrderService.entity.Status;
import com.example.OrderService.service.OrderService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentConsumer {

    private final OrderService orderService;

    @KafkaListener(topics = "CREATE_PAYMENT", groupId = "order-group")
    public void listen(PaymentEvent event) {
        log.info("Получено событие платежа: {}", event);

        try {
            if ("SUCCESS".equals(event.getStatus())) {
                orderService.setStatus(event.getOrderId(), Status.PAID);
                log.info("Заказ {} успешно оплачен", event.getOrderId());
            } else {
                orderService.setStatus(event.getOrderId(), Status.CANCELLED);
                log.info("Оплата заказа {} отменена", event.getOrderId());
            }
        } catch (EntityNotFoundException e) {
            log.warn(" Не удалось обновить статус. Заказ {} не найден в базе.", event.getOrderId());
        } catch (Exception e) {
            log.error(" Ошибка при обработке заказа {}", event.getOrderId(), e);
        }
    }
}