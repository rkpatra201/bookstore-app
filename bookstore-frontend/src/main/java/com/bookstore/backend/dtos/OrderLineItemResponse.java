package com.bookstore.backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderLineItemResponse {
    private Long orderId;
    private int itemId;
    private String title;
    private double unitPrice;
    private int quantity;
    private double subTotal;
}
