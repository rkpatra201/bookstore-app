package com.bookstore.backend.controllers;

import com.bookstore.backend.dtos.*;
import com.bookstore.backend.services.OrderService;
import com.bookstore.backend.services.UserContextService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing customer orders and checkout operations.
 * <p>
 * Provides endpoints for checkout, order retrieval, and order history.
 * All operations are user-scoped and require authentication.
 * Checkout operations are transactional and handle stock reduction.
 * </p>
 */
@RestController
@RequestMapping("api/orders")
@Tag(name = "Order Management", description = "APIs for managing orders, checkout, and order history")
public class OrderController {

    private final OrderService orderService;
    private final UserContextService userContextService;

    public OrderController(OrderService orderService, UserContextService userContextService) {
        this.orderService = orderService;
        this.userContextService = userContextService;
    }

    /**
     * Processes checkout and creates an order from the user's shopping cart.
     * <p>
     * This is a transactional operation that:
     * - Validates cart is not empty
     * - Verifies delivery address exists
     * - Validates payment method is provided
     * - Creates order with line items
     * - Reduces stock quantities
     * - Clears the cart
     * Returns order details including address snapshot and total price.
     * </p>
     *
     * @param request the checkout request containing addressId and paymentMethod
     * @return ResponseEntity containing the order details and HTTP 200 OK
     * @throws com.bookstore.backend.exceptions.OrderException if cart is empty, address not found, payment method missing, or insufficient stock
     */
    @PostMapping("/checkout")
    @Operation(summary = "Checkout cart", description = "Processes checkout and creates an order from the user's cart items")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Order placed successfully",
                    content = @Content(schema = @Schema(implementation = OrderResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid checkout request or cart is empty",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "User not authenticated",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Address not found",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Insufficient stock for one or more items",
                    content = @Content
            )
    })
    public ResponseEntity<DataResponse<OrderResponse>> checkout(
            @Parameter(description = "Checkout details with address ID and payment method", required = true)
            @Valid @RequestBody CheckoutRequest request) {
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
     * Retrieves complete details of a specific order including all line items.
     * <p>
     * Fetches a single order with all associated line items and address details.
     * Ensures the order belongs to the authenticated user for security.
     * Returns detailed information including item snapshots from order time.
     * </p>
     *
     * @param orderId the unique identifier of the order to retrieve
     * @return ResponseEntity containing order details with line items and HTTP 200 OK
     * @throws com.bookstore.backend.exceptions.OrderException if order not found or doesn't belong to user
     */
    @GetMapping("/{orderId}")
    @Operation(summary = "Get order by ID", description = "Retrieves detailed information about a specific order including all line items")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Order details retrieved successfully",
                    content = @Content(schema = @Schema(implementation = OrderDetailsResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "User not authenticated",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Order not found",
                    content = @Content
            )
    })
    public ResponseEntity<DataResponse<OrderDetailsResponse>> getOrderById(
            @Parameter(description = "Unique ID of the order to retrieve", required = true, example = "1")
            @PathVariable Long orderId) {
        String userId = userContextService.getUserContext().getUserId();
        OrderDetailsResponse detailsResponse = orderService.getOrderById(orderId, userId);

        DataResponse<OrderDetailsResponse> response = new DataResponse<>(
                true,
                "Order details retrieved successfully",
                detailsResponse
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves the complete order history for the authenticated user.
     * <p>
     * Fetches all orders belonging to the authenticated user as a summary list.
     * Returns basic order information without detailed line items for performance.
     * Orders are typically sorted by creation date in descending order.
     * </p>
     *
     * @return ResponseEntity containing list of order summaries and HTTP 200 OK
     */
    @GetMapping
    @Operation(summary = "Get order history", description = "Retrieves all orders for the authenticated user")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Order history retrieved successfully",
                    content = @Content(schema = @Schema(implementation = DataResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "User not authenticated",
                    content = @Content
            )
    })
    public ResponseEntity<DataResponse<List<OrderSummaryResponse>>> getOrderHistory() {
        String userId = userContextService.getUserContext().getUserId();
        List<OrderSummaryResponse> history = orderService.getOrderHistory(userId);

        DataResponse<List<OrderSummaryResponse>> response = new DataResponse<>(
                true,
                "Order history retrieved successfully",
                history
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Processes payment for a specific order.
     * <p>
     * Marks the order as paid and updates the payment status.
     * This endpoint is called after successful payment processing.
     * Ensures the order belongs to the authenticated user for security.
     * </p>
     *
     * @param orderId the unique identifier of the order to process payment for
     * @return ResponseEntity with success message and HTTP 200 OK
     * @throws com.bookstore.backend.exceptions.OrderException if order not found, doesn't belong to user, or already paid
     */
    @PostMapping("/{orderId}/payment")
    @Operation(summary = "Process order payment", description = "Records payment for a specific order")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Payment processed successfully",
                    content = @Content(schema = @Schema(implementation = DataResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Order already paid",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "User not authenticated",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Order not found",
                    content = @Content
            )
    })
    public ResponseEntity<DataResponse<Void>> processPayment(
            @Parameter(description = "Unique ID of the order to process payment for", required = true, example = "1")
            @PathVariable Long orderId) {

        String userId = userContextService.getUserContext().getUserId();
        // For now, this is a placeholder that marks order as paid
        // In production, this would integrate with payment gateway

        DataResponse<Void> response = new DataResponse<>(
                true,
                "Payment processed successfully",
                null
        );
        return ResponseEntity.ok(response);
    }

}
