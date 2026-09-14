package com.bookstore.backend.enums;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum UserAccountError {
    EMAIL_REQUIRED("Email is required", HttpStatus.BAD_REQUEST.value()),
    FIRST_NAME_REQUIRED("First name is required", HttpStatus.BAD_REQUEST.value()),
    PASSWORD_REQUIRED("Password is required", HttpStatus.BAD_REQUEST.value()),
    EMAIL_ALREADY_EXISTS("Email is already registered", HttpStatus.CONFLICT.value()),
    USER_NOT_FOUND("User account not found", HttpStatus.NOT_FOUND.value()),
    USER_NOT_FOUND_WITH_EMAIL("User account not found with email: %s", HttpStatus.NOT_FOUND.value()),
    INVALID_EMAIL_FORMAT("Invalid email format", HttpStatus.BAD_REQUEST.value()),
    LOGIN_FAILED("Login attempt failed", HttpStatus.UNAUTHORIZED.value());

    private final String message;
    private final int httpCode;

    UserAccountError(String message, int httpCode) {
        this.message = message;
        this.httpCode = httpCode;
    }

    public String getFormattedMessage(Object... args) {
        return String.format(message, args);
    }
}
