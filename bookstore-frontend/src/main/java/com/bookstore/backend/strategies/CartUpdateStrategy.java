package com.bookstore.backend.strategies;

import com.bookstore.backend.dtos.LineItemRequest;

public interface CartUpdateStrategy {
    // Determines which strategy to execute based on whether delta is positive or negative
    boolean isCartUpdateAllowed(int delta);

    void update(String userId, LineItemRequest request);
}
