package com.bookstore.backend.controllers;

import com.bookstore.backend.dtos.CheckoutRequest;
import com.bookstore.backend.dtos.DataResponse;
import com.bookstore.backend.dtos.OrderDetailsResponse;
import com.bookstore.backend.dtos.OrderResponse;
import com.bookstore.backend.services.OrderService;
import com.bookstore.backend.services.UserContextService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final UserContextService userContextService;

    public OrderController(OrderService orderService, UserContextService userContextService) {
        this.orderService = orderService;
        this.userContextService = userContextService;
    }

    /**
     * Executes a transactional checkout by turning an active cart into a finalized order.
     */
    @PostMapping("/checkout")
    public ResponseEntity<DataResponse<OrderResponse>> checkout(@RequestBody CheckoutRequest request) {
        String userId = userContextService.getUserContext().getUserId();
        OrderResponse orderResponse = orderService.checkout(userId, request);

        DataResponse<OrderResponse> response = new DataResponse<>(
                true,
                "Order placed successfully",
                orderResponse
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Fetches details of a specific order along with its item snapshot records.
     * Enforces secure data privacy via user context checks.
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<DataResponse<OrderDetailsResponse>> getOrderById(@PathVariable Long orderId) {
        String userId = userContextService.getUserContext().getUserId();
        OrderDetailsResponse detailsResponse = orderService.getOrderById(orderId, userId);

        DataResponse<OrderDetailsResponse> response = new DataResponse<>(
                true,
                "Order details retrieved successfully",
                detailsResponse
        );
        return ResponseEntity.ok(response);
    }
}
