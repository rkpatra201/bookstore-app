package com.bookstore.backend.services;

import com.bookstore.backend.dtos.Book;
import com.bookstore.backend.dtos.Cart;
import com.bookstore.backend.dtos.LineItemRequest;
import com.bookstore.backend.dtos.LineItemResponse;
import com.bookstore.backend.entities.CartLineItemEntity;
import com.bookstore.backend.mappers.LineItemRequestMapper;
import com.bookstore.backend.repositories.CartRepository;
import com.bookstore.backend.strategies.CartUpdateStrategy;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final List<CartUpdateStrategy> cartUpdateStrategies;
    private BookService bookService;
    private LineItemRequestMapper lineItemRequestMapper = LineItemRequestMapper.INSTANCE;

    // Direct constructor injection
    public CartService(CartRepository cartRepository, BookService bookService, List<CartUpdateStrategy> cartUpdateStrategies) {
        this.cartRepository = cartRepository;
        this.bookService = bookService;
        this.cartUpdateStrategies = cartUpdateStrategies;
    }

    public Cart getCart(String userId) {
        List<LineItemResponse> lineItemRespons = getCartItemsByUserId(userId);
        return Cart.builder()
                .userId(userId)
                .lineItems(lineItemRespons)
                .totalCartPrice(lineItemRespons.stream()
                        .mapToDouble(LineItemResponse::getSubTotal)
                        .sum()).build();
    }

    private List<LineItemResponse> getCartItemsByUserId(String userId) {
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

                    LineItemResponse response = LineItemResponse.builder()
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

    public Cart updateItemQuantity(String userId, LineItemRequest lineItemRequest) {
        int delta = lineItemRequest.getQuantity();

        if (delta == 0) {
            throw new IllegalArgumentException("Quantity cannot be zero");
        }

        CartUpdateStrategy strategy = null;
        // Locate the matching strategy layout at runtime dynamically
        for (CartUpdateStrategy cartUpdateStrategy : cartUpdateStrategies) {
            if (cartUpdateStrategy.isCartUpdateAllowed(delta)) {
                strategy = cartUpdateStrategy;
                break;
            }
        }

        if (strategy == null) {
            throw new IllegalArgumentException("Unsupported quantity change operation");
        }

        // Execute strategy behavior
        strategy.update(userId, lineItemRequest);

        // Always re-render and return updated cart snapshot
        return getCart(userId);
    }


    /**
     * Wipes out all active line items inside the target customer's shopping cart.
     */
    public void clearCart(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }

        cartRepository.clearCart(userId);
    }


}
