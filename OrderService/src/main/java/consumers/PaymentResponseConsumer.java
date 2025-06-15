package main.java.consumers;

import main.java.events.PaymentResponseEvent;
import main.java.services.OrderService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentResponseConsumer {

    private final OrderService orderService;

    public PaymentResponseConsumer(OrderService orderService) {
        this.orderService = orderService;
    }

    @KafkaListener(topics = "payment-response")
    public void handlePaymentResponse(PaymentResponseEvent event) {
        orderService.processPaymentResponse(event);
    }
}