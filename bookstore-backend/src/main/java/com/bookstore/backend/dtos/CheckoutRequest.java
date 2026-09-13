package com.bookstore.backend.dtos;

import com.bookstore.backend.enums.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutRequest {
    private Long addressId;
    private PaymentMethod paymentMethod;
}
