package com.bookstore.backend.enums;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum AddressError {
    ADDRESS_NOT_FOUND("Address not found or access denied", HttpStatus.NOT_FOUND.value()),
    UPDATE_FAILED("Failed to update address. Address not found or access denied", HttpStatus.NOT_FOUND.value()),
    DELETE_FAILED("Failed to delete address. Address not found or access denied", HttpStatus.NOT_FOUND.value());

    private final String message;
    private final int httpCode;

    AddressError(String message, int httpCode) {
        this.message = message;
        this.httpCode = httpCode;
    }

    public String getFormattedMessage(Object... args) {
        return String.format(message, args);
    }
}
