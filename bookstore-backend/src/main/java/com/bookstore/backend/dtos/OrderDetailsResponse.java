package com.bookstore.backend.dtos;

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
public class OrderDetailsResponse {
    private Long id;
    private String shippingAddressSnapshot;
    private double totalAmount;
    private OrderStatus orderStatus;
    private PaymentMethod paymentMethod;
    private LocalDateTime createdAt;
    private List<OrderLineItemResponse> lineItems;
}
