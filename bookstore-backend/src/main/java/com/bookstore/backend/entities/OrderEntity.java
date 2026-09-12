package com.bookstore.backend.entities;

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
    private String orderStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<OrderLineItemEntity> lineItems; // Embedded detail list
}
