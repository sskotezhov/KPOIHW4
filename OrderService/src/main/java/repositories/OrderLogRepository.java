package main.java.repositories;
	
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import main.java.models.OrderLog;


@Repository
public interface OrderLogRepository extends JpaRepository<OrderLog, String> {
	List<OrderLog> findByPublishedFalse();
	Optional<OrderLog> findById(String orderId);
}
