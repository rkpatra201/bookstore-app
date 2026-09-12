package com.bookstore.backend.services;

import com.bookstore.backend.dtos.Book;
import com.bookstore.backend.dtos.LineItemRequest;
import com.bookstore.backend.entities.CartLineItemEntity;
import com.bookstore.backend.mappers.LineItemRequestMapper;
import com.bookstore.backend.repositories.CartRepository;
import org.springframework.stereotype.Service;

@Service
public class CartService {

    private BookService bookService;
    private final CartRepository cartRepository;

    private LineItemRequestMapper lineItemRequestMapper = LineItemRequestMapper.INSTANCE;

    // Direct constructor injection
    public CartService(CartRepository cartRepository, BookService bookService) {
        this.cartRepository = cartRepository;
        this.bookService = bookService;
    }


    public void addItemToCart(String userId, LineItemRequest request) {
        validateCartRequest(request);

        CartLineItemEntity entity = lineItemRequestMapper.toEntity(request, userId);
        cartRepository.saveOrUpdate(entity);
    }

    // todo: better exception handling
    private void validateCartRequest(LineItemRequest request) {
        if (request.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        Book book = this.bookService.getBookById(request.getItemId());
        if (book.getStockQty() < request.getQuantity()) {
            throw new IllegalArgumentException("Required stock is not available for item: " + request.getItemId());
        }
    }

}
