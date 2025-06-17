package main.java.events;

import java.math.BigDecimal;

public class OrderCreatedEvent extends OrderEvent {
	public OrderCreatedEvent(String userId, String orderId, BigDecimal Amount)
	{
		this.setUserId(userId);
		this.setOrderId(orderId);
		this.setAmount(Amount);
	};
}