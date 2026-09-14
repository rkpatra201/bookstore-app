package com.bookstore.backend.enums;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum CartError {
    INVALID_REQUEST("Cart request cannot be null", HttpStatus.BAD_REQUEST.value()),
    INVALID_ITEM_ID("Invalid item ID", HttpStatus.BAD_REQUEST.value()),
    INVALID_QUANTITY("Quantity must be greater than zero", HttpStatus.BAD_REQUEST.value()),
    QUANTITY_CANNOT_BE_ZERO("Quantity cannot be zero", HttpStatus.BAD_REQUEST.value()),
    INSUFFICIENT_STOCK("Insufficient stock available for item: %s. Available: %d, Requested: %d", HttpStatus.CONFLICT.value()),
    ITEM_NOT_FOUND("Item not found in your cart", HttpStatus.NOT_FOUND.value()),
    UNSUPPORTED_OPERATION("Unsupported quantity change operation", HttpStatus.BAD_REQUEST.value()),
    INVALID_USER_ID("User ID cannot be null or empty", HttpStatus.BAD_REQUEST.value());

    private final String message;
    private final int httpCode;

    CartError(String message, int httpCode) {
        this.message = message;
        this.httpCode = httpCode;
    }

    public String getFormattedMessage(Object... args) {
        return String.format(message, args);
    }
}
