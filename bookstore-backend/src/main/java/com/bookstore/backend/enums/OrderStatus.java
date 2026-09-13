package com.bookstore.backend.enums;

public enum OrderStatus {
    RESERVED("Reserved - Awaiting Payment"),
    AWAITING_PAYMENT("Awaiting Payment"),
    PAYMENT_SUCCESS("Payment Successful"),
    PAYMENT_ERROR("Payment Error"),
    PAYMENT_TIMEOUT("Payment Timeout"),
    CANCELLED("Order Cancelled"),
    REFUND_INITIATED("Refund Initiated"),
    REFUND_ERROR("Refund Error"),
    REFUND_COMPLETE("Refund Complete");

    private final String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
