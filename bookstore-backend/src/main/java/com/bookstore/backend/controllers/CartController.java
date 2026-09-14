package com.bookstore.backend.controllers;

import com.bookstore.backend.dtos.Cart;
import com.bookstore.backend.dtos.DataResponse;
import com.bookstore.backend.dtos.LineItemRequest;
import com.bookstore.backend.services.CartService;
import com.bookstore.backend.services.UserContextService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing shopping cart operations.
 * <p>
 * Provides endpoints for adding, removing, updating, and retrieving cart items
 * for authenticated users. All operations are user-scoped and require authentication.
 * </p>
 */
@RestController
@RequestMapping("api/cart")
@Tag(name = "Shopping Cart Management", description = "APIs for managing user shopping cart items")
public class CartController {

    private final CartService cartService;
    private final UserContextService userContextService;

    public CartController(CartService cartService, UserContextService userContextService) {
        this.cartService = cartService;
        this.userContextService = userContextService;
    }

    /**
     * Adds a new item to the authenticated user's shopping cart.
     * <p>
     * If the item already exists in the cart, the quantity will be incremented.
     * Validates stock availability before adding to cart.
     * Returns the complete updated cart state after the operation.
     * </p>
     *
     * @param request the line item request containing itemId and quantity
     * @return ResponseEntity containing the updated cart with all items and HTTP 200 OK
     * @throws com.bookstore.backend.exceptions.CartException if validation fails or stock is insufficient
     */
    @PostMapping
    @Operation(summary = "Add item to cart", description = "Adds a book to the user's shopping cart or increments quantity if already present")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Item added successfully",
                    content = @Content(schema = @Schema(implementation = Cart.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid request data or insufficient stock",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "User not authenticated",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Insufficient stock available",
                    content = @Content
            )
    })
    public ResponseEntity<DataResponse<Cart>> addToCart(
            @Parameter(description = "Line item details with book ID and quantity", required = true)
            @Valid @RequestBody LineItemRequest request) {

        String userId = userContextService.getUserContext().getUserId();
        cartService.addItemToCart(userId, request);

        // Fetch updated cart to return in response
        Cart updatedCart = cartService.getCart(userId);

        DataResponse<Cart> response = new DataResponse<>(
                true,
                "Item successfully added to your cart",
                updatedCart
        );

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Removes an item completely from the user's shopping cart.
     * <p>
     * Deletes the entire line item regardless of quantity.
     * Returns the complete updated cart state after the operation.
     * </p>
     *
     * @param itemId the unique identifier of the item to remove
     * @return ResponseEntity containing the updated cart with all items and HTTP 200 OK
     * @throws com.bookstore.backend.exceptions.CartException if item not found in cart
     */
    @DeleteMapping("/{itemId}")
    @Operation(summary = "Remove item from cart", description = "Completely removes a specific item from the shopping cart")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Item removed successfully",
                    content = @Content(schema = @Schema(implementation = Cart.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "User not authenticated",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Item not found in cart",
                    content = @Content
            )
    })
    public ResponseEntity<DataResponse<Cart>> removeItemFromCart(
            @Parameter(description = "ID of the item to remove from cart", required = true, example = "123")
            @PathVariable int itemId) {

        String userId = userContextService.getUserContext().getUserId();
        cartService.removeItemFromCart(userId, itemId);

        // Fetch updated cart to return in response
        Cart updatedCart = cartService.getCart(userId);

        DataResponse<Cart> response = new DataResponse<>(
                true,
                "Item successfully removed from your cart",
                updatedCart
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Updates the quantity of a specific item in the shopping cart.
     * <p>
     * Supports both increment and decrement operations based on the quantity delta.
     * Returns the updated cart state after the operation.
     * </p>
     *
     * @param itemId the unique identifier of the item to update
     * @param request the line item request with quantity change
     * @return ResponseEntity containing the updated cart and HTTP 200 OK
     * @throws com.bookstore.backend.exceptions.CartException if item not found or invalid quantity
     */
    @PatchMapping("/{itemId}")
    @Operation(summary = "Update item quantity", description = "Modifies the quantity of an item in the cart (increment or decrement)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Quantity updated successfully",
                    content = @Content(schema = @Schema(implementation = Cart.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid quantity value",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "User not authenticated",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Item not found in cart",
                    content = @Content
            )
    })
    public ResponseEntity<DataResponse<Cart>> updateItemQuantity(
            @Parameter(description = "ID of the item to update", required = true, example = "123")
            @PathVariable int itemId,
            @Parameter(description = "Updated quantity details", required = true)
            @Valid @RequestBody LineItemRequest request) {

        request.setItemId(itemId);
        String userId = userContextService.getUserContext().getUserId();
        Cart updatedCart = cartService.updateItemQuantity(userId, request);

        DataResponse<Cart> response = new DataResponse<>(
                true,
                "Cart quantity updated successfully",
                updatedCart
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves the complete shopping cart for the authenticated user.
     * <p>
     * Returns cart with all line items, including calculated prices and totals.
     * </p>
     *
     * @return ResponseEntity containing the user's cart with all items and HTTP 200 OK
     */
    @GetMapping
    @Operation(summary = "Get shopping cart", description = "Fetches the complete cart with all items, quantities, and calculated totals")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Cart retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Cart.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "User not authenticated",
                    content = @Content
            )
    })
    public ResponseEntity<DataResponse<Cart>> getCart() {
        String userId = userContextService.getUserContext().getUserId();
        Cart cartResponse = cartService.getCart(userId);

        DataResponse<Cart> response = new DataResponse<>(
                true,
                "Cart retrieved successfully",
                cartResponse
        );

        return ResponseEntity.ok(response);
    }
}
