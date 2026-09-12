package com.bookstore.backend.entities;

import lombok.Data;

/**
 * Maps directly to 'cart_items' table.
 */
@Data
public class CartLineItemEntity {
    private int id;         // PK Auto-increment
    private String userId;   // Associates item to a specific session/user
    private Integer itemId;  // FK pointing to book table
    private int quantity;
    // Constructors, Getters, and Setters
}
