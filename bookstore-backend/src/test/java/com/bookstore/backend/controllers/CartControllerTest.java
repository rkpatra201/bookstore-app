package com.bookstore.backend.controllers;

import com.bookstore.backend.dtos.DataResponse;
import com.bookstore.backend.dtos.LineItemRequest;
import com.bookstore.backend.dtos.UserContext; // Assuming your UserContext class package
import com.bookstore.backend.services.CartService;
import com.bookstore.backend.services.UserContextService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class CartControllerTest {

    @Mock
    private CartService cartService;

    @Mock
    private UserContextService userContextService;

    @InjectMocks
    private CartController cartController;

    private static final String USER_ID = "user-123";
    private static final int ITEM_ID = 999;
    private static final int QUANTITY = 2;

    @Test
    void addToCart_shouldReturnOkStatusAndValidResponse_whenItemAddedSuccessfully() {
        // Arrange
        LineItemRequest request = new LineItemRequest();
        request.setItemId(ITEM_ID);
        request.setQuantity(QUANTITY);

        UserContext mockContext = Mockito.mock(UserContext.class);
        Mockito.when(mockContext.getUserId()).thenReturn(USER_ID);
        Mockito.when(userContextService.getUserContext()).thenReturn(mockContext);

        // Act
        ResponseEntity<DataResponse<Void>> responseEntity = cartController.addToCart(request);

        // Assert
        Assertions.assertNotNull(responseEntity);
        Assertions.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        DataResponse<Void> body = responseEntity.getBody();
        Assertions.assertNotNull(body);
        Assertions.assertTrue(body.isSuccess()); // Assuming getSuccess() or isSuccess() exists
        Assertions.assertEquals("Item successfully added to your cart", body.getMessage());
        Assertions.assertNull(body.getData());

        // Verify downstream service layer interactions
        Mockito.verify(userContextService, Mockito.times(1)).getUserContext();
        Mockito.verify(cartService, Mockito.times(1)).addItemToCart(USER_ID, request);
    }

    @Test
    void removeItemFromCart_shouldReturnOkStatus_whenDeletionIsSuccessful() {
        // Arrange: Setup mock user context lookup step-by-step
        UserContext mockContext = Mockito.mock(UserContext.class);
        Mockito.when(mockContext.getUserId()).thenReturn(USER_ID);
        Mockito.when(userContextService.getUserContext()).thenReturn(mockContext);

        // Mock the void service method call behavior explicitly to do nothing
        Mockito.doNothing().when(cartService).removeItemFromCart(USER_ID, ITEM_ID);

        // Act: Directly execute controller endpoint function
        ResponseEntity<DataResponse<Void>> responseEntity = cartController.removeItemFromCart(ITEM_ID);

        // Assert: Validate responses match structural schemas
        Assertions.assertNotNull(responseEntity);
        Assertions.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        DataResponse<Void> body = responseEntity.getBody();
        Assertions.assertNotNull(body);
        Assertions.assertTrue(body.isSuccess());
        Assertions.assertEquals("Item successfully removed from your cart", body.getMessage());
        Assertions.assertNull(body.getData());

        // Verify downstream layer communication occurred correctly
        Mockito.verify(userContextService, Mockito.times(1)).getUserContext();
        Mockito.verify(cartService, Mockito.times(1)).removeItemFromCart(USER_ID, ITEM_ID);
    }
}
