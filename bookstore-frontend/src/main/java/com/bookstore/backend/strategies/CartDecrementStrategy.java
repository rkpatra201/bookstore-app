package com.bookstore.backend.strategies;

import com.bookstore.backend.dtos.LineItemRequest;
import com.bookstore.backend.repositories.CartRepository;
import org.springframework.stereotype.Component;

@Component
public class CartDecrementStrategy implements CartUpdateStrategy {

    private final CartRepository cartRepository;

    public CartDecrementStrategy(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    @Override
    public boolean isCartUpdateAllowed(int delta) {
        return delta < 0;
    }

    @Override
    public void update(String userId, LineItemRequest request) {
        int quantityToReduce = Math.abs(request.getQuantity());
        cartRepository.reduceItemCount(userId, request.getItemId(), quantityToReduce);
    }
}
