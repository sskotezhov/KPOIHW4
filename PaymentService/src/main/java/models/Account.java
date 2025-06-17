package main.java.models;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "accounts")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Account {
	public Account(String id, BigDecimal balance) {
		this.id = id;
		this.balance = balance;
	}

	@Id
	@Column(name = "user_id")
	private String id;

	@Column(nullable = false, precision = 19, scale = 4)
	private BigDecimal balance;
}
