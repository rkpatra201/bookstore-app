package com.bookstore.backend.exceptions;

import com.bookstore.backend.enums.OrderError;
import lombok.Getter;

@Getter
public class OrderException extends RuntimeException {
    private final OrderError error;

    public OrderException(OrderError error) {
        super(error.getMessage());
        this.error = error;
    }

    public OrderException(OrderError error, Object... args) {
        super(error.getFormattedMessage(args));
        this.error = error;
    }

    public int getHttpCode() {
        return error.getHttpCode();
    }
}
