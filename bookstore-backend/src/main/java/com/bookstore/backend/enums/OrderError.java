package com.bookstore.backend.enums;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum OrderError {
    INSUFFICIENT_STOCK("Insufficient stock for item: %s. Available: %d, Requested: %d", HttpStatus.CONFLICT.value()),
    STOCK_UPDATE_FAILED("Failed to update stock quantity. Item may be out of stock", HttpStatus.CONFLICT.value()),
    ORDER_NOT_FOUND("Order not found", HttpStatus.NOT_FOUND.value()),
    EMPTY_CART("Cannot checkout an empty shopping cart", HttpStatus.BAD_REQUEST.value()),
    PAYMENT_METHOD_REQUIRED("Payment method is required for checkout", HttpStatus.BAD_REQUEST.value()),
    INVALID_USER_ID("User ID cannot be null or empty", HttpStatus.BAD_REQUEST.value()),
    FAILED_TO_RETRIEVE_ORDER_ID("Failed to retrieve generated order ID", HttpStatus.INTERNAL_SERVER_ERROR.value()),
    PRIMARY_KEY_NOT_FOUND("Primary key 'id' not found in generated keys map", HttpStatus.INTERNAL_SERVER_ERROR.value());

    private final String message;
    private final int httpCode;

    OrderError(String message, int httpCode) {
        this.message = message;
        this.httpCode = httpCode;
    }

    public String getFormattedMessage(Object... args) {
        return String.format(message, args);
    }
}
