package com.bookstore.backend.exceptions;

import com.bookstore.backend.enums.CartError;
import lombok.Getter;

@Getter
public class CartException extends RuntimeException {
    private final CartError error;

    public CartException(CartError error) {
        super(error.getMessage());
        this.error = error;
    }

    public CartException(CartError error, Object... args) {
        super(error.getFormattedMessage(args));
        this.error = error;
    }

    public int getHttpCode() {
        return error.getHttpCode();
    }
}
