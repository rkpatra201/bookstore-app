package com.bookstore.backend.services;

import com.bookstore.backend.dtos.Cart;
import com.bookstore.backend.dtos.LineItemRequest;
import com.bookstore.backend.strategies.CartUpdateStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class CartServiceStrategyTest {

    private CartUpdateStrategy incrementStrategy;
    private CartUpdateStrategy decrementStrategy;
    private CartService cartService;

    private static final String USER_ID = "user-123";

    @BeforeEach
    void setUp() {
        incrementStrategy = Mockito.mock(CartUpdateStrategy.class);
        decrementStrategy = Mockito.mock(CartUpdateStrategy.class);
        
        // Mock application scopes
        // Build service manually with list of mocked strategies
        cartService = Mockito.spy(new CartService(null, null, List.of(incrementStrategy, decrementStrategy)));
    }

    @Test
    void updateItemQuantity_shouldInvokeIncrementStrategy_whenDeltaIsPositive() {
        // Arrange
        Mockito.when(incrementStrategy.isCartUpdateAllowed(Mockito.anyInt())).thenAnswer(inv -> (int)inv.getArgument(0) > 0);
        LineItemRequest request = new LineItemRequest(501, 2); // Delta = +2
        Mockito.doReturn(Cart.builder().build()).when(cartService).getCart(USER_ID);

        // Act
        cartService.updateItemQuantity(USER_ID, request);

        // Assert
        Mockito.verify(incrementStrategy, Mockito.times(1)).update(USER_ID, request);
        Mockito.verify(decrementStrategy, Mockito.never()).update(Mockito.any(), Mockito.any());
    }

    @Test
    void updateItemQuantity_shouldInvokeDecrementStrategy_whenDeltaIsNegative() {
        // Arrange
        Mockito.when(decrementStrategy.isCartUpdateAllowed(Mockito.anyInt())).thenAnswer(inv -> (int)inv.getArgument(0) < 0);

        LineItemRequest request = new LineItemRequest(501, -3); // Delta = -3
        Mockito.doReturn(Cart.builder().build()).when(cartService).getCart(USER_ID);

        // Act
        cartService.updateItemQuantity(USER_ID, request);

        // Assert
        Mockito.verify(decrementStrategy, Mockito.times(1)).update(USER_ID, request);
        Mockito.verify(incrementStrategy, Mockito.never()).update(Mockito.any(), Mockito.any());
    }
}
