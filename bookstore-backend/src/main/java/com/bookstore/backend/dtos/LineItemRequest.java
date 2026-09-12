package com.bookstore.backend.dtos;

import lombok.Data;

/**
 * Payload to add a book to the cart or modify its current quantity.
 * Endpoints: POST /api/cart, PUT /api/cart
 */
@Data
public class LineItemRequest {
    private Integer itemId;
    private int quantity;
}
