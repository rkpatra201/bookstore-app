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
    REFUND_COMPLETE("Refund Complete"),

    // Order Fulfillment Statuses
    PROCESSING("Processing Order"),
    PACKED("Packed and Ready"),
    SHIPPED("Shipped"),
    OUT_FOR_DELIVERY("Out for Delivery"),
    DELIVERED("Delivered"),
    DELIVERY_FAILED("Delivery Failed"),

    // Post-Delivery Statuses
    COMPLETED("Order Completed"),
    RETURN_REQUESTED("Return Requested"),
    RETURN_APPROVED("Return Approved"),
    RETURN_REJECTED("Return Rejected"),
    RETURN_IN_TRANSIT("Return in Transit"),
    RETURN_RECEIVED("Return Received"),
    RETURNED("Returned"),

    // Additional Edge Cases
    ON_HOLD("On Hold"),
    PARTIALLY_SHIPPED("Partially Shipped"),
    EXCHANGE_REQUESTED("Exchange Requested"),
    DISPUTED("Disputed");

    private final String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
