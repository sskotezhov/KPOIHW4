package main.java.controllers;

import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import main.java.models.Order;
import main.java.services.OrderService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Order API", description = "API для управления заказами")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @Operation(summary = "Создать заказ")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Заказ создан",
                    content = @Content(schema = @Schema(implementation = Order.class))),
        @ApiResponse(responseCode = "400", description = "Неверные параметры")
    })
    public ResponseEntity<Order> createOrder(
            @Parameter(description = "ID пользователя", required = true)
            @RequestHeader("user_id") String userId,
            
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Сумма заказа",
                required = true,
                content = @Content(schema = @Schema(implementation = BigDecimal.class)))
            @RequestBody BigDecimal amount) {
        
        Order order = orderService.createOrder(userId, amount);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Получить заказ по ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Заказ найден",
                    content = @Content(schema = @Schema(implementation = Order.class))),
        @ApiResponse(responseCode = "404", description = "Заказ не найден")
    })
    public ResponseEntity<Order> getOrder(
            @Parameter(description = "ID заказа", required = true)
            @PathVariable String orderId) {
        
        return ResponseEntity.ok(orderService.getOrder(orderId));
    }

    @GetMapping
    @Operation(summary = "Получить все заказы пользователя")
    @ApiResponse(responseCode = "200", description = "Список заказов",
                content = @Content(array = @ArraySchema(schema = @Schema(implementation = Order.class))))
    public ResponseEntity<List<Order>> getUserOrders(
            @Parameter(description = "ID пользователя", required = true)
            @RequestHeader("user_id") String userId) {
        
        return ResponseEntity.ok(orderService.getUserOrders(userId));
    }
}