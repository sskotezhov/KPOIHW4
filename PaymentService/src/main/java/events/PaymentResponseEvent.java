package main.java.events;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaymentResponseEvent {
	private String orderId;
	private boolean success;
	private String message;
}