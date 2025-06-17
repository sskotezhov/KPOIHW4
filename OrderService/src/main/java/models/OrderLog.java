package main.java.models;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "order_log")
@NoArgsConstructor
public class OrderLog {
	@Id
	private String orderId;
	private boolean published;
	
}
