package com.bookstore.backend.services;

import com.bookstore.backend.dtos.Book;
import com.bookstore.backend.dtos.Cart;
import com.bookstore.backend.dtos.LineItemRequest;
import com.bookstore.backend.dtos.LineItemResponse;
import com.bookstore.backend.entities.CartLineItemEntity;
import com.bookstore.backend.mappers.LineItemRequestMapper;
import com.bookstore.backend.repositories.CartRepository;
import com.bookstore.backend.strategies.CartUpdateStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
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
        log.debug("Fetching cart for user: {}", userId);
        List<LineItemResponse> lineItemResponse = getCartItemsByUserId(userId);
        Cart cart = Cart.builder()
                .userId(userId)
                .lineItems(lineItemResponse)
                .itemCount(lineItemResponse.stream().mapToInt(LineItemResponse::getQuantity).sum())
                .totalCartPrice(lineItemResponse.stream()
                        .mapToDouble(LineItemResponse::getSubTotal)
                        .sum()).build();
        log.info("Cart retrieved for user: {} - {} items, total: ${}", userId, cart.getItemCount(), String.format("%.2f", cart.getTotalCartPrice()));
        return cart;
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
        log.info("Adding item to cart - User: {}, ItemId: {}, Quantity: {}", userId, request.getItemId(), request.getQuantity());

        try {
            validateCartRequest(request);
            CartLineItemEntity entity = lineItemRequestMapper.toEntity(request, userId);
            cartRepository.saveOrUpdate(entity);

            log.info("Successfully added item to cart - User: {}, ItemId: {}, Quantity: {}", userId, request.getItemId(), request.getQuantity());
        } catch (Exception e) {
            log.error("Failed to add item to cart - User: {}, ItemId: {}, Error: {}", userId, request.getItemId(), e.getMessage());
            throw e;
        }
    }

    private void validateCartRequest(LineItemRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Cart request cannot be null");
        }

        if (request.getItemId() == null || request.getItemId() <= 0) {
            throw new IllegalArgumentException("Invalid item ID");
        }

        if (request.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        Book book = this.bookService.getBookById(request.getItemId());
        if (book.getStockQty() < request.getQuantity()) {
            throw new IllegalArgumentException("Insufficient stock available for item: " + book.getTitle() + ". Available: " + book.getStockQty() + ", Requested: " + request.getQuantity());
        }
    }

    public void removeItemFromCart(String userId, int itemId) {
        log.info("Removing item from cart - User: {}, ItemId: {}", userId, itemId);

        boolean deleted = cartRepository.deleteItem(userId, itemId);

        if (!deleted) {
            log.warn("Failed to remove item from cart - User: {}, ItemId: {} - Item not found", userId, itemId);
            throw new IllegalArgumentException("Item not found in your cart");
        }

        log.info("Successfully removed item from cart - User: {}, ItemId: {}", userId, itemId);
    }

    public Cart updateItemQuantity(String userId, LineItemRequest lineItemRequest) {
        log.info("Updating cart item quantity - User: {}, ItemId: {}, Delta: {}", userId, lineItemRequest.getItemId(), lineItemRequest.getQuantity());

        int delta = lineItemRequest.getQuantity();

        if (delta == 0) {
            log.warn("Cart update failed - User: {}, ItemId: {} - Quantity cannot be zero", userId, lineItemRequest.getItemId());
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
            log.error("Cart update failed - User: {}, ItemId: {} - Unsupported quantity change operation", userId, lineItemRequest.getItemId());
            throw new IllegalArgumentException("Unsupported quantity change operation");
        }

        try {
            // Execute strategy behavior
            strategy.update(userId, lineItemRequest);
            log.info("Successfully updated cart item quantity - User: {}, ItemId: {}, Delta: {}", userId, lineItemRequest.getItemId(), delta);
        } catch (Exception e) {
            log.error("Failed to update cart item quantity - User: {}, ItemId: {}, Error: {}", userId, lineItemRequest.getItemId(), e.getMessage());
            throw e;
        }

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

        log.info("Clearing cart for user: {}", userId);
        cartRepository.clearCart(userId);
        log.info("Successfully cleared cart for user: {}", userId);
    }


}
