package main.java.events;

import lombok.Data;
import java.math.BigDecimal;

@Data
public abstract class OrderEvent {
    private String orderId;
    private String userId;
    private BigDecimal amount;
}