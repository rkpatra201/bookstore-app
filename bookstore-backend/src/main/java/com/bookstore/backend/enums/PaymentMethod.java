package com.bookstore.backend.enums;

public enum PaymentMethod {
    COD("Cash on Delivery"),
    UPI("UPI Payment"),
    CC("Credit Card"),
    DC("Debit Card"),
    BANK("Bank Transfer");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
