package com.bookstore.backend.services;

import com.bookstore.backend.dtos.Book;
import com.bookstore.backend.dtos.Cart;
import com.bookstore.backend.dtos.LineItemRequest;
import com.bookstore.backend.dtos.LineItemResponse;
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

import java.util.Collections;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    private static final String USER_ID = "user-123";
    private static final int ITEM_ID = 999;
    @Mock
    private CartRepository cartRepository;
    @Mock
    private BookService bookService;
    @InjectMocks
    private CartService cartService;

    @Test
    void addItemToCart_shouldThrowException_whenQuantityIsZero() {
        // Arrange
        LineItemRequest request = new LineItemRequest(ITEM_ID, 0);

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
        LineItemRequest request = new LineItemRequest(ITEM_ID, -5);

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
        LineItemRequest request = new LineItemRequest(ITEM_ID, 10);

        // Using Builder pattern for only required fields
        Book bookMock = Book.builder()
                .id(ITEM_ID)
                .title("Test Book")
                .stockQty(9)
                .build();

        Mockito.when(bookService.getBookById(ITEM_ID)).thenReturn(bookMock);

        // Act & Assert
        IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> cartService.addItemToCart(USER_ID, request)
        );

        Assertions.assertEquals("Insufficient stock available for item: Test Book. Available: 9, Requested: 10", exception.getMessage());

        Mockito.verify(cartRepository, Mockito.never()).saveOrUpdate(Mockito.any(CartLineItemEntity.class));
    }

    @Test
    void addItemToCart_shouldSaveSuccessfully_whenQuantityAndStockAreValid() {
        // Arrange
        LineItemRequest request = new LineItemRequest(ITEM_ID, 5);

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
        LineItemRequest request = new LineItemRequest(ITEM_ID, 3);

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

    @Test
    void getCart_shouldReturnCartResponseWithZeroPrice_whenCartIsEmpty() {
        // Arrange
        Mockito.when(cartRepository.findByUserId(USER_ID)).thenReturn(Collections.emptyList());

        // Act
        Cart response = cartService.getCart(USER_ID);

        // Assert
        Assertions.assertNotNull(response);
        Assertions.assertEquals(USER_ID, response.getUserId());
        Assertions.assertTrue(response.getLineItems().isEmpty());
        Assertions.assertEquals(0.0, response.getTotalCartPrice());

        Mockito.verifyNoInteractions(bookService);
    }

    @Test
    void getCart_shouldReturnPopulatedCartResponse_whenItemsExist() {
        // Arrange: Mock 2 raw items inside the database cart repository
        CartLineItemEntity entity1 = new CartLineItemEntity();
        entity1.setItemId(101);
        entity1.setQuantity(2);

        CartLineItemEntity entity2 = new CartLineItemEntity();
        entity2.setItemId(102);
        entity2.setQuantity(1);

        Mockito.when(cartRepository.findByUserId(USER_ID)).thenReturn(List.of(entity1, entity2));

        // Arrange: Mock the metadata details from BookService using Lombok Builder
        Book book1 = Book.builder()
                .id(101)
                .title("Spring Guide")
                .price(25.0f)
                .build();

        Book book2 = Book.builder()
                .id(102)
                .title("Testing Guide")
                .price(40.0f)
                .build();

        Mockito.when(bookService.getBookById(101)).thenReturn(book1);
        Mockito.when(bookService.getBookById(102)).thenReturn(book2);

        // Act
        Cart response = cartService.getCart(USER_ID);

        // Assert: Verify metadata composition fields
        Assertions.assertNotNull(response);
        Assertions.assertEquals(USER_ID, response.getUserId());
        Assertions.assertEquals(2, response.getLineItems().size());

        // Verify Total Math Calculation: (25.0 * 2) + (40.0 * 1) = 90.0
        Assertions.assertEquals(90.0, response.getTotalCartPrice());

        // Verify individual line item breakdowns
        LineItemResponse res1 = response.getLineItems().get(0);
        Assertions.assertEquals("Spring Guide", res1.getTitle());
        Assertions.assertEquals(50.0, res1.getSubTotal());

        LineItemResponse res2 = response.getLineItems().get(1);
        Assertions.assertEquals("Testing Guide", res2.getTitle());
        Assertions.assertEquals(40.0, res2.getSubTotal());
    }

    @Test
    void removeItemFromCart_shouldExecuteSuccessfully_whenItemExistsAndIsDeleted() {
        // Arrange: Mock the repository to report a successful deletion (returns true)
        Mockito.when(cartRepository.deleteItem(USER_ID, ITEM_ID)).thenReturn(true);

        // Act & Assert: Verify no exception is thrown during execution
        Assertions.assertDoesNotThrow(() -> cartService.removeItemFromCart(USER_ID, ITEM_ID));

        // Verify downstream repository method was called exactly once with accurate bounds
        Mockito.verify(cartRepository, Mockito.times(1)).deleteItem(USER_ID, ITEM_ID);
    }

    @Test
    void removeItemFromCart_shouldThrowIllegalArgumentException_whenItemDoesNotExist() {
        // Arrange: Mock the repository to report deletion failure (returns false)
        Mockito.when(cartRepository.deleteItem(USER_ID, ITEM_ID)).thenReturn(false);

        // Act & Assert: Verify that the expected exception is thrown
        IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> cartService.removeItemFromCart(USER_ID, ITEM_ID)
        );

        // Verify the descriptive message text contract matches exactly
        Assertions.assertEquals("Item not found in your cart", exception.getMessage());

        // Verify repository method execution occurred
        Mockito.verify(cartRepository, Mockito.times(1)).deleteItem(USER_ID, ITEM_ID);
    }


    @Test
    void clearCart_shouldThrowIllegalArgumentException_whenUserIdIsEmpty() {
        // Act & Assert (Blank context safety validation check)
        IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> cartService.clearCart("")
        );

        Assertions.assertEquals("User ID cannot be null or empty", exception.getMessage());
        Mockito.verifyNoInteractions(cartRepository);
    }

    @Test
    void clearCart_shouldInvokeRepositorySuccessfully_whenUserIdIsValid() {
        // Arrange
        Mockito.doNothing().when(cartRepository).clearCart(USER_ID);

        // Act & Assert
        Assertions.assertDoesNotThrow(() -> cartService.clearCart(USER_ID));

        // Verify downstream repository layer communication occurred exactly once
        Mockito.verify(cartRepository, Mockito.times(1)).clearCart(USER_ID);
    }

}
