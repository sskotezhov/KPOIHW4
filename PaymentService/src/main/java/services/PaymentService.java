package main.java.services;

import java.math.BigDecimal;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import main.java.events.OrderCreatedEvent;
import main.java.events.PaymentResponseEvent;
import main.java.models.Account;
import main.java.models.PaymentLog;
import main.java.repositories.AccountRepository;
import main.java.repositories.PaymentLogRepository;

@Service
public class PaymentService {
	private final AccountRepository accountRepository;
	private final PaymentLogRepository paymentLogRepository;
	private final KafkaTemplate<String, PaymentResponseEvent> kafkaTemplate;


	public PaymentService(AccountRepository accountRepository, PaymentLogRepository paymentLogRepository, KafkaTemplate<String, PaymentResponseEvent> kafkaTemplate) {
		this.accountRepository = accountRepository;
		this.paymentLogRepository = paymentLogRepository;
		this.kafkaTemplate = kafkaTemplate;
	}

	public Account createAccount(String userId, BigDecimal balance) {
		if (accountRepository.existsById(userId)) {
			throw new IllegalStateException("Account already exists for user: " + userId);
		}

		Account newAccount = new Account(userId, balance);
		return accountRepository.save(newAccount);
	}

	public Account deposit(String userId, BigDecimal amount) {
		Account account = accountRepository.findById(userId)
				.orElseThrow(() -> new RuntimeException("Account not found"));

		account.setBalance(account.getBalance().add(amount));
		return accountRepository.save(account);
	}

	@Transactional
	public boolean processPayment(String userId, BigDecimal amount, String orderId) {
		if (paymentLogRepository.existsByOrderId(orderId)) {
			return false;
		}

		Account account = accountRepository.findById(userId).get();
		if (account.getBalance().compareTo(amount) < 0) {
			PaymentLog log = new PaymentLog(orderId, PaymentLog.Status.FAILED);
			paymentLogRepository.save(log);
			return false;
		}
		BigDecimal newBalance = account.getBalance().subtract(amount);
		account.setBalance(newBalance);
		accountRepository.save(account);
		PaymentLog log = new PaymentLog(orderId, PaymentLog.Status.PAID);
		paymentLogRepository.save(log);

		return true;
	}

	@Transactional(readOnly = true)
	public BigDecimal getAccountBalance(String userId) {
		Objects.requireNonNull(userId, "User ID cannot be null");

		return accountRepository.findById(userId).map(Account::getBalance)
				.orElseThrow(() -> new IllegalArgumentException("Account not found for user: " + userId));
	}
	
	@KafkaListener(topics = "order-created", groupId = "order-service-group", containerFactory = "kafkaListenerContainerFactory")
	public void handle(OrderCreatedEvent event) {
		 System.out.println(event.getOrderId());
		 try 
		 { 
			 if (paymentLogRepository.existsByOrderId(event.getOrderId())) {return;}
		 
		 boolean success = processPayment(event.getUserId(), event.getAmount(), event.getOrderId());
		 
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
