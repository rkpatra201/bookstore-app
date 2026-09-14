package com.bookstore.backend.exceptions;

import com.bookstore.backend.enums.AddressError;
import lombok.Getter;

@Getter
public class AddressException extends RuntimeException {
    private final AddressError error;

    public AddressException(AddressError error) {
        super(error.getMessage());
        this.error = error;
    }

    public AddressException(AddressError error, Object... args) {
        super(error.getFormattedMessage(args));
        this.error = error;
    }

    public int getHttpCode() {
        return error.getHttpCode();
    }
}
