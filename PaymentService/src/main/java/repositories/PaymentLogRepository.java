package main.java.repositories;

import main.java.models.PaymentLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentLogRepository extends JpaRepository<PaymentLog, String> {
	boolean existsByOrderId(String orderId);
}