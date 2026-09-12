package com.bookstore.backend.services;

import com.bookstore.backend.dtos.Book;
import com.bookstore.backend.dtos.Cart;
import com.bookstore.backend.dtos.LineItemRequest;
import com.bookstore.backend.dtos.LineItemOutput;
import com.bookstore.backend.entities.CartLineItemEntity;
import com.bookstore.backend.mappers.LineItemRequestMapper;
import com.bookstore.backend.repositories.CartRepository;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public Cart getCart(String userId) {
        List<LineItemOutput> lineItemRespons = getCartItemsByUserId(userId);
        return Cart.builder()
                .userId(userId)
                .lineItems(lineItemRespons)
                .totalCartPrice(lineItemRespons.stream()
                        .mapToDouble(LineItemOutput::getSubTotal)
                        .sum()).build();
    }

    private List<LineItemOutput> getCartItemsByUserId(String userId) {
        // 1. Fetch all raw cart records from the repository
        List<CartLineItemEntity> cartEntities = cartRepository.findByUserId(userId);

        // 2. Map and enrich each entry with product metadata
        return cartEntities.stream()
                .map(entity -> {
                    // Fetch book details to populate dynamic information like title and unitPrice
                    Book book = bookService.getBookById(entity.getItemId());

                    double unitPrice = book.getPrice();
                    int quantity = entity.getQuantity();
                    double subTotal = unitPrice * quantity; // Compute dynamically

                    LineItemOutput response = LineItemOutput.builder()
                            .subTotal(subTotal)
                            .unitPrice(unitPrice)
                            .quantity(quantity)
                            .itemId(book.getId())
                            .title(book.getTitle())
                            .build();

                    return response;
                })
                .toList();
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

    public void removeItemFromCart(String userId, int itemId) {
        boolean deleted = cartRepository.deleteItem(userId, itemId);

        if (!deleted) {
            throw new IllegalArgumentException("Item not found in your cart");
        }
    }


}
