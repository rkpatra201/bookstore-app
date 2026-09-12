package com.bookstore.backend.controllers;

import com.bookstore.backend.dtos.DataResponse;
import com.bookstore.backend.dtos.LineItemRequest;
import com.bookstore.backend.services.CartService;
import com.bookstore.backend.services.UserContextService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/cart")
public class CartController {

    private final CartService cartService;
    private final UserContextService userContextService;

    public CartController(CartService cartService, UserContextService userContextService) {
        this.cartService = cartService;
        this.userContextService = userContextService;
    }

    @PostMapping
    public ResponseEntity<DataResponse<Void>> addToCart(
            @RequestBody LineItemRequest request) {

        cartService.addItemToCart(userContextService.getUserContext().getUserId(), request);

        DataResponse<Void> response = new DataResponse<>(
                true, 
                "Item successfully added to your cart", 
                null
        );
        
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<DataResponse<Void>> removeItemFromCart(@PathVariable int itemId) {
        // 1. Fetch user context identity
        String userId = userContextService.getUserContext().getUserId();

        // 2. Delegate deletion logic execution to the service layer
        cartService.removeItemFromCart(userId, itemId);

        // 3. Construct a standard API response wrap
        DataResponse<Void> response = new DataResponse<>(
                true,
                "Item successfully removed from your cart",
                null
        );

        return ResponseEntity.ok(response);
    }

}
