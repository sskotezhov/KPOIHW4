package main.java.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import main.java.models.Account;

@Repository
public interface AccountRepository extends JpaRepository<Account, String> {
	Optional<Account> findById(String id);
}
