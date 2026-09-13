package com.bookstore.backend.exceptions;

import com.bookstore.backend.enums.UserAccountError;
import lombok.Getter;

@Getter
public class UserAccountException extends RuntimeException {
    private final UserAccountError error;

    public UserAccountException(UserAccountError error) {
        super(error.getMessage());
        this.error = error;
    }

    public UserAccountException(UserAccountError error, Object... args) {
        super(error.getFormattedMessage(args));
        this.error = error;
    }

    public int getHttpCode() {
        return error.getHttpCode();
    }
}
