package main.java.services;

import main.java.events.*;
import main.java.models.*;
import main.java.repositories.OrderLogRepository;
import main.java.repositories.OrderRepository;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderLogRepository orderLogRepository;
    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    public OrderService(OrderRepository orderRepository, 
                      KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate,
                      OrderLogRepository orderLogRepository) {
        this.orderLogRepository = orderLogRepository;
		this.orderRepository =		 orderRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    public Order createOrder(String userId, BigDecimal amount) {
        Order order = new Order(UUID.randomUUID().toString(), userId, amount, Order.Status.CREATED);
        order = orderRepository.save(order);

        OrderLog outboxEvent = new OrderLog();
        outboxEvent.setOrderId(order.getId());
        outboxEvent.setPublished(false);
        orderLogRepository.save(outboxEvent);
        
        return order;
    }
    @KafkaListener(topics = "payment-response")
    @Transactional
    public void processPaymentResponse(PaymentResponseEvent event) {
        try {
        	System.out.println(event.getOrderId());
            Order order = orderRepository.findById(event.getOrderId())
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));
            
            order.setStatus(event.isSuccess() ? Order.Status.PAID : Order.Status.FAILED);
            orderRepository.save(order);
            
        } catch (EntityNotFoundException e) {
            System.out.println(e);
        }
    }

    @Scheduled(fixedRate = 5000)
    @Transactional
    public void processOutbox() {
        orderLogRepository.findByPublishedFalse()
            .forEach(outbox -> {
                Order order = orderRepository.findById(outbox.getOrderId())
                    .orElseThrow(() -> new EntityNotFoundException("Order not found"));
                System.out.println(order.getId());
                try {
                    OrderCreatedEvent event = new OrderCreatedEvent(
                        order.getUserId(),
                        order.getId(),
                        order.getAmount()
                    );
                    
                    kafkaTemplate.send("order-created", event);
                    outbox.setPublished(true);
                    orderLogRepository.save(outbox);
                } catch (Exception e) {
                    System.out.println("Failed to publish order {}" + outbox.getOrderId() + e);
                }
            });
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