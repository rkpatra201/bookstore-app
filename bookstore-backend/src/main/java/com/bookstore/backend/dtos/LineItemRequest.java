package com.bookstore.backend.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Payload to add a book to the cart or modify its current quantity.
 * Endpoints: POST /api/cart, PUT /api/cart
 */
@Data
@AllArgsConstructor
public class LineItemRequest {
    @NotNull(message = "Item ID is required")
    @Min(value = 1, message = "Item ID must be positive")
    private Integer itemId;

    @NotNull(message = "Quantity is required")
    private Integer quantity;
}
