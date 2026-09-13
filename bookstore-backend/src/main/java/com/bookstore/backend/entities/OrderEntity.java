package com.bookstore.backend.entities;

import com.bookstore.backend.enums.OrderStatus;
import com.bookstore.backend.enums.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEntity {
    private Long id;
    private String userId;
    private String shippingAddressSnapshot; // The frozen text copy of the address
    private double totalAmount;
    private OrderStatus orderStatus;
    private PaymentMethod paymentMethod;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<OrderLineItemEntity> lineItems; // Embedded detail list
}
