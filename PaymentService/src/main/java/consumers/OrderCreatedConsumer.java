package main.java.consumers;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import lombok.NoArgsConstructor;
import main.java.events.OrderCreatedEvent;
import main.java.events.PaymentResponseEvent;
import main.java.repositories.PaymentLogRepository;
import main.java.services.PaymentService;

@Component
public class OrderCreatedConsumer {

	
	private final PaymentService paymentService;private final
	KafkaTemplate<String, PaymentResponseEvent> kafkaTemplate;
	private final PaymentLogRepository paymentLogRepository;

	
	 public OrderCreatedConsumer(
			 PaymentService paymentService,
			 KafkaTemplate<String, PaymentResponseEvent> kafkaTemplate, 
			 PaymentLogRepository paymentLogRepository) 
	 { this.paymentService = paymentService;
	 this.kafkaTemplate = kafkaTemplate; this.paymentLogRepository =
	 paymentLogRepository; }
	 

	@KafkaListener(topics = "order-created", groupId = "order-service-group", containerFactory = "kafkaListenerContainerFactory")
	public void handle(OrderCreatedEvent event) {
		
		 try 
		 { 
			 if (paymentLogRepository.existsByOrderId(event.getOrderId())) {return;}
		 
		 boolean success = paymentService.processPayment( event.getUserId(),
		 event.getAmount(), event.getOrderId() );
		 
		 kafkaTemplate.send("payment-response", new PaymentResponseEvent(
				 		 event.getOrderId(), 
						 success, 
						 success ? "Success" :"Insufficient funds") ); 
		 } 
		 catch (Exception e) 
		 {
			 kafkaTemplate.send(
					 "payment-response", 
					 new PaymentResponseEvent(
							 event.getOrderId(), 
							 false,
							 e.getMessage())); 
		 }
		 
	}
}