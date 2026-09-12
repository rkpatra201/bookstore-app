package com.bookstore.backend.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LineItemOutput {
    private Integer itemId;
    private String title;
    private double unitPrice;
    private int quantity;
    private double subTotal; // Computed: unitPrice * quantity

    // Constructors, Getters, Setters
}
