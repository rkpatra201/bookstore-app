package com.bookstore.backend.strategies;

import com.bookstore.backend.dtos.Book;
import com.bookstore.backend.dtos.LineItemRequest;
import com.bookstore.backend.entities.CartLineItemEntity;
import com.bookstore.backend.enums.CartError;
import com.bookstore.backend.exceptions.CartException;
import com.bookstore.backend.mappers.LineItemRequestMapper;
import com.bookstore.backend.repositories.CartRepository;
import com.bookstore.backend.services.BookService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CartIncrementStrategy implements CartUpdateStrategy {

    private final CartRepository cartRepository;
    private final BookService bookService;
    private final LineItemRequestMapper lineItemRequestMapper = LineItemRequestMapper.INSTANCE;

    public CartIncrementStrategy(CartRepository cartRepository, BookService bookService) {
        this.cartRepository = cartRepository;
        this.bookService = bookService;
    }

    @Override
    public boolean isCartUpdateAllowed(int delta) {
        return delta > 0;
    }

    @Override
    public void update(String userId, LineItemRequest request) {
        Book book = bookService.getBookById(request.getItemId());
        List<CartLineItemEntity> existingItems = cartRepository.findByUserId(userId);

        int currentQtyInCart = existingItems.stream()
                .filter(item -> item.getItemId() == request.getItemId())
                .mapToInt(CartLineItemEntity::getQuantity)
                .findFirst()
                .orElse(0);

        if (book.getStockQty() < (currentQtyInCart + request.getQuantity())) {
            throw new CartException(CartError.INSUFFICIENT_STOCK, book.getTitle(), book.getStockQty(), currentQtyInCart + request.getQuantity());
        }

        CartLineItemEntity entity = lineItemRequestMapper.toEntity(request, userId);
        cartRepository.saveOrUpdate(entity);
    }
}
