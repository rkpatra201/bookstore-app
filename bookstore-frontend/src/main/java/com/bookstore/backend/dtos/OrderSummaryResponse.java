package com.bookstore.backend.dtos;

import com.bookstore.backend.enums.OrderStatus;
import com.bookstore.backend.enums.PaymentMethod;
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
    private OrderStatus orderStatus;
    private PaymentMethod paymentMethod;
    private LocalDateTime createdAt;
}
