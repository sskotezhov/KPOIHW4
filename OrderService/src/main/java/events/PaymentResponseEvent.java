package main.java.events;

import lombok.Data;

@Data
public class PaymentResponseEvent {
    private String orderId;
    private boolean success;
    private String message;
}