package main.java.services;

import main.java.events.*;
import main.java.models.*;
import main.java.repositories.OrderRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    public OrderService(OrderRepository orderRepository, 
                      KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate) {
        this.orderRepository =		 orderRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    public Order createOrder(String userId, BigDecimal amount) {
        Order order = new Order(UUID.randomUUID().toString(), userId, amount, Order.Status.CREATED);
        order = orderRepository.save(order);

        OrderCreatedEvent event = new OrderCreatedEvent();
        event.setOrderId(order.getId());
        event.setUserId(userId);
        event.setAmount(amount);

        kafkaTemplate.send("order-created", event);
        return order;
    }

    @Transactional
    public void processPaymentResponse(PaymentResponseEvent event) {
        Order order = orderRepository.findById(event.getOrderId())
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        order.setStatus(event.isSuccess() ? Order.Status.PAID : Order.Status.FAILED);
        orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public Order getOrder(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));
    }

    @Transactional(readOnly = true)
    public List<Order> getUserOrders(String userId) {
        return orderRepository.findByUserId(userId);
    }
}