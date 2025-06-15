package main.java.models;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Data
@Entity
@Table(name = "processed_payments")
public class PaymentLog {

	public enum Status {
		CREATED, PROCESSING, PAID, FAILED
	};

	@Id
	private String orderId;
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private Status status;
}