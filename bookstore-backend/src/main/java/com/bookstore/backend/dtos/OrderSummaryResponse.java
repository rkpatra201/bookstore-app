package com.bookstore.backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderSummaryResponse {
    private Long id;
    private String shippingAddressSnapshot;
    private double totalAmount;
    private String orderStatus;
    private LocalDateTime createdAt;
}
