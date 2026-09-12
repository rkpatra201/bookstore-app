package com.bookstore.backend.services;

import com.bookstore.backend.dtos.Book;
import com.bookstore.backend.dtos.LineItemRequest;
import com.bookstore.backend.entities.CartLineItemEntity;
import com.bookstore.backend.repositories.CartRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private BookService bookService;

    @InjectMocks
    private CartService cartService;

    private static final String USER_ID = "user-123";
    private static final int ITEM_ID = 999;

    @Test
    void addItemToCart_shouldThrowException_whenQuantityIsZero() {
        // Arrange
        LineItemRequest request = new LineItemRequest();
        request.setItemId(ITEM_ID);
        request.setQuantity(0);

        // Act & Assert
        IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> cartService.addItemToCart(USER_ID, request)
        );

        Assertions.assertEquals("Quantity must be greater than zero", exception.getMessage());

        Mockito.verifyNoInteractions(bookService);
        Mockito.verifyNoInteractions(cartRepository);
    }

    @Test
    void addItemToCart_shouldThrowException_whenQuantityIsNegative() {
        // Arrange
        LineItemRequest request = new LineItemRequest();
        request.setItemId(ITEM_ID);
        request.setQuantity(-5);

        // Act & Assert
        IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> cartService.addItemToCart(USER_ID, request)
        );

        Assertions.assertEquals("Quantity must be greater than zero", exception.getMessage());

        Mockito.verifyNoInteractions(bookService);
        Mockito.verifyNoInteractions(cartRepository);
    }

    @Test
    void addItemToCart_shouldThrowException_whenStockIsInsufficient() {
        // Arrange
        LineItemRequest request = new LineItemRequest();
        request.setItemId(ITEM_ID);
        request.setQuantity(10);

        // Using Builder pattern for only required fields
        Book bookMock = Book.builder()
                .id(ITEM_ID)
                .stockQty(9)
                .build();

        Mockito.when(bookService.getBookById(ITEM_ID)).thenReturn(bookMock);

        // Act & Assert
        IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> cartService.addItemToCart(USER_ID, request)
        );

        Assertions.assertEquals("Required stock is not available for item: " + ITEM_ID, exception.getMessage());

        Mockito.verify(cartRepository, Mockito.never()).saveOrUpdate(Mockito.any(CartLineItemEntity.class));
    }

    @Test
    void addItemToCart_shouldSaveSuccessfully_whenQuantityAndStockAreValid() {
        // Arrange
        LineItemRequest request = new LineItemRequest();
        request.setItemId(ITEM_ID);
        request.setQuantity(5);

        // Using Builder pattern for only required fields
        Book bookMock = Book.builder()
                .id(ITEM_ID)
                .stockQty(5)
                .build();

        Mockito.when(bookService.getBookById(ITEM_ID)).thenReturn(bookMock);

        // Act
        cartService.addItemToCart(USER_ID, request);

        // Assert
        ArgumentCaptor<CartLineItemEntity> entityCaptor = ArgumentCaptor.forClass(CartLineItemEntity.class);
        Mockito.verify(cartRepository, Mockito.times(1)).saveOrUpdate(entityCaptor.capture());

        CartLineItemEntity savedEntity = entityCaptor.getValue();
        Assertions.assertNotNull(savedEntity);
        Assertions.assertEquals(USER_ID, savedEntity.getUserId());
        Assertions.assertEquals(ITEM_ID, savedEntity.getItemId());
        Assertions.assertEquals(5, savedEntity.getQuantity());
    }

    @Test
    void addItemToCart_shouldSaveSuccessfully_whenStockIsAbundant() {
        // Arrange
        LineItemRequest request = new LineItemRequest();
        request.setItemId(ITEM_ID);
        request.setQuantity(3);

        // Using Builder pattern for only required fields
        Book bookMock = Book.builder()
                .id(ITEM_ID)
                .stockQty(100)
                .build();

        Mockito.when(bookService.getBookById(ITEM_ID)).thenReturn(bookMock);

        // Act
        cartService.addItemToCart(USER_ID, request);

        // Assert
        ArgumentCaptor<CartLineItemEntity> entityCaptor = ArgumentCaptor.forClass(CartLineItemEntity.class);
        Mockito.verify(cartRepository, Mockito.times(1)).saveOrUpdate(entityCaptor.capture());

        CartLineItemEntity savedEntity = entityCaptor.getValue();
        Assertions.assertEquals(USER_ID, savedEntity.getUserId());
        Assertions.assertEquals(ITEM_ID, savedEntity.getItemId());
        Assertions.assertEquals(3, savedEntity.getQuantity());
    }
}
