package com.bookstore.backend.dtos;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class Cart {
    private String userId;
    private List<LineItemOutput> lineItems;
    private double totalCartPrice; // Computed sum of all sub-totals
}
