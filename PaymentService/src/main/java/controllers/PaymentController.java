package main.java.controllers;

import java.math.BigDecimal;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityExistsException;
import main.java.models.Account;
import main.java.services.PaymentService;

@RestController
@RequestMapping("/api/payments")
@Tag(name = "Payment API", description = "API для управления платежами и счетами пользователей")
public class PaymentController {

	private final PaymentService paymentService;

	public PaymentController(PaymentService paymentService) {
		this.paymentService = paymentService;
	}

	@PostMapping("/accounts")
	@Operation(summary = "Создать новый счет", description = "Создает счет для пользователя с начальным балансом 0", parameters = {
			@Parameter(name = "user_id", description = "Уникальный идентификатор пользователя", required = true, example = "user123") }, responses = {
					@ApiResponse(responseCode = "200", description = "Счет успешно создан", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Account.class))),
					@ApiResponse(responseCode = "400", description = "Неверный запрос", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(type = "string", example = "User ID cannot be null"))),
					@ApiResponse(responseCode = "409", description = "Счет уже существует", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(type = "string", example = "Account already exists"))) })
	public ResponseEntity<?> createAccount(@RequestHeader("user_id") String userId) {
		try {
			Account account = paymentService.createAccount(userId, BigDecimal.ZERO);
			return ResponseEntity.ok(account);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		} catch (IllegalStateException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
		}
	}

	@PostMapping("/deposit")
	@Operation(summary = "Пополнить счет", description = "Пополняет баланс счета на указанную сумму", parameters = {
			@Parameter(name = "user_id", description = "Уникальный идентификатор пользователя", required = true, example = "user123") }, requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Сумма для пополнения", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(type = "number", format = "decimal", example = "100.50"))), responses = {
					@ApiResponse(responseCode = "200", description = "Баланс успешно пополнен", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Account.class))),
					@ApiResponse(responseCode = "400", description = "Неверная сумма", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(type = "string", example = "Amount must be positive"))),
					@ApiResponse(responseCode = "404", description = "Счет не найден", content = @Content) })
	public ResponseEntity<?> deposit(@RequestHeader("user_id") String userId, @RequestBody BigDecimal amount) {
		try {
			Account account = paymentService.deposit(userId, amount);
			return ResponseEntity.ok(account);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		} catch (EntityExistsException e) {
			return ResponseEntity.notFound().build();
		}
	}

	@GetMapping("/balance")
	@Operation(summary = "Получить баланс счета", description = "Возвращает текущий баланс счета пользователя", parameters = {
			@Parameter(name = "user_id", description = "Уникальный идентификатор пользователя", required = true, example = "user123") }, responses = {
					@ApiResponse(responseCode = "200", description = "Баланс получен", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(type = "number", format = "decimal", example = "1500.75"))),
					@ApiResponse(responseCode = "400", description = "Неверный ID пользователя", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(type = "string", example = "User ID cannot be empty"))),
					@ApiResponse(responseCode = "404", description = "Счет не найден", content = @Content) })
	public ResponseEntity<?> getBalance(@RequestHeader("user_id") String userId) {
		try {
			BigDecimal balance = paymentService.getAccountBalance(userId);
			return ResponseEntity.ok(balance);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		} catch (EntityExistsException e) {
			return ResponseEntity.notFound().build();
		}
	}
}