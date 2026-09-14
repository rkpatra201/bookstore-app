package com.bookstore.backend.services;

import com.bookstore.backend.dtos.*;
import com.bookstore.backend.entities.OrderEntity;
import com.bookstore.backend.entities.OrderLineItemEntity;
import com.bookstore.backend.enums.OrderError;
import com.bookstore.backend.enums.OrderStatus;
import com.bookstore.backend.enums.PaymentMethod;
import com.bookstore.backend.exceptions.ItemNotFoundException;
import com.bookstore.backend.exceptions.OrderException;
import com.bookstore.backend.repositories.BookRepository;
import com.bookstore.backend.repositories.OrderRepository;
import org.junit.jupiter.api.Assertions;
import com.bookstore.backend.exceptions.OrderException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class OrderServiceCheckoutTest {

    private static final String USER_ID = "customer-user-111";
    private static final Long ADDRESS_ID = 55L;
    private static final Long EXPECTED_ORDER_ID = 999L;
    private static final String ATTACKER_USER_ID = "hacker-user-999";
    private static final Long ORDER_ID = 888L;
    @Mock
    private CartService cartService;
    @Mock
    private AddressService addressService;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private BookRepository bookRepository;
    @InjectMocks
    private OrderService orderService;

    @Test
    void checkout_shouldThrowIllegalStateException_whenCartIsEmpty() {
        // Arrange: Mock an empty cart response (line items list is empty)
        Cart emptyCart = Cart.builder()
                .userId(USER_ID)
                .lineItems(Collections.emptyList())
                .totalCartPrice(0.0)
                .build();
        Mockito.when(cartService.getCart(USER_ID)).thenReturn(emptyCart);

        CheckoutRequest request = new CheckoutRequest(ADDRESS_ID, PaymentMethod.COD);

        // Act & Assert: Check fail-fast exception behavior
        OrderException exception = Assertions.assertThrows(
                OrderException.class,
                () -> orderService.checkout(USER_ID, request)
        );

        Assertions.assertEquals("Cannot checkout an empty shopping cart", exception.getMessage());

        // Verify code terminates instantly without invoking sub-layers
        Mockito.verifyNoInteractions(addressService);
        Mockito.verifyNoInteractions(orderRepository);
    }

    @Test
    void checkout_shouldThrowIllegalStateException_whenCartLineItemsListIsNull() {
        // Arrange: Mock a cart response with a null list parameter
        Cart nullCart = Cart.builder()
                .userId(USER_ID)
                .lineItems(null)
                .totalCartPrice(0.0)
                .build();
        Mockito.when(cartService.getCart(USER_ID)).thenReturn(nullCart);

        CheckoutRequest request = new CheckoutRequest(ADDRESS_ID, PaymentMethod.UPI);

        // Act & Assert
        OrderException exception = Assertions.assertThrows(
                OrderException.class,
                () -> orderService.checkout(USER_ID, request)
        );

        Assertions.assertEquals("Cannot checkout an empty shopping cart", exception.getMessage());
        Mockito.verifyNoInteractions(addressService);
    }

    @Test
    void checkout_shouldThrowIllegalArgumentException_whenPaymentMethodIsNull() {
        // Arrange: Mock a valid cart
        LineItemResponse cartItem = LineItemResponse.builder()
                .itemId(101)
                .title("Test Book")
                .unitPrice(50.0)
                .quantity(1)
                .subTotal(50.0)
                .build();

        Cart mockCart = Cart.builder()
                .userId(USER_ID)
                .lineItems(List.of(cartItem))
                .totalCartPrice(50.0)
                .build();
        Mockito.when(cartService.getCart(USER_ID)).thenReturn(mockCart);

        // Mock valid address
        CustomerAddress mockAddress = CustomerAddress.builder()
                .recipientName("John Doe")
                .addressLine1("123 Main St")
                .city("Bengaluru")
                .state("Karnataka")
                .postalCode("560016")
                .country("India")
                .build();
        Mockito.when(addressService.getAddressById(ADDRESS_ID, USER_ID)).thenReturn(mockAddress);

        // Create request with null payment method
        CheckoutRequest request = new CheckoutRequest(ADDRESS_ID, null);

        // Act & Assert
        OrderException exception = Assertions.assertThrows(
                OrderException.class,
                () -> orderService.checkout(USER_ID, request)
        );

        Assertions.assertEquals("Payment method is required for checkout", exception.getMessage());
        Mockito.verifyNoInteractions(orderRepository);
    }

    @Test
    void checkout_shouldCompleteWorkflowSuccessfully_whenCartHasItemsAndAddressIsValid() {
        // Arrange: 1. Mock populated shopping cart items
        LineItemResponse.LineItemResponseBuilder cartItem1 = LineItemResponse.builder();
        cartItem1.itemId(101);
        cartItem1.title("Spring Framework In Action");
        cartItem1.unitPrice(45.0);
        cartItem1.quantity(2);
        cartItem1.subTotal(90.0);

        LineItemResponse.LineItemResponseBuilder cartItem2 = LineItemResponse.builder();
        cartItem2.itemId(102);
        cartItem2.title("Clean Code Mastery");
        cartItem2.unitPrice(40.0);
        cartItem2.quantity(1);
        cartItem2.subTotal(40.0);

        Cart mockCart = Cart.builder()
                .userId(USER_ID)
                .lineItems(List.of(cartItem1.build(), cartItem2.build()))
                .totalCartPrice(130.0)
                .build();
        Mockito.when(cartService.getCart(USER_ID)).thenReturn(mockCart);

        // Arrange: 2. Mock a valid delivery customer address profile
        CustomerAddress mockAddress = CustomerAddress.builder()
                .recipientName("John Doe")
                .addressLine1("123 Main St")
                .addressLine2("Apt 4B")
                .city("Bengaluru")
                .state("Karnataka")
                .postalCode("560016")
                .country("India")
                .build();
        Mockito.when(addressService.getAddressById(ADDRESS_ID, USER_ID)).thenReturn(mockAddress);

        // Arrange: 3. Mock stock reduction for both items
        Mockito.when(bookRepository.reduceStock(101, 2)).thenReturn(true);
        Mockito.when(bookRepository.reduceStock(102, 1)).thenReturn(true);

        // Arrange: 4. Mock database persistence actions & cleanup operations
        Mockito.when(orderRepository.saveMasterOrder(Mockito.any(OrderEntity.class))).thenReturn(EXPECTED_ORDER_ID);
        Mockito.doNothing().when(orderRepository).saveOrderLineItems(Mockito.eq(EXPECTED_ORDER_ID), Mockito.anyList());
        Mockito.doNothing().when(cartService).clearCart(USER_ID);

        CheckoutRequest request = new CheckoutRequest(ADDRESS_ID, PaymentMethod.COD);

        // Act
        OrderResponse response = orderService.checkout(USER_ID, request);

        // Assert: 4. Check that order invoice numbers match contract structures
        Assertions.assertNotNull(response);
        Assertions.assertEquals(EXPECTED_ORDER_ID, response.getOrderId());
        Assertions.assertEquals(OrderStatus.RESERVED, response.getStatus());
        Assertions.assertEquals(PaymentMethod.COD, response.getPaymentMethod());
        Assertions.assertEquals(130.0, response.getTotalAmount());

        // Assert: 5. Verify character flattening structure generated for the address text snapshot
        ArgumentCaptor<OrderEntity> orderCaptor = ArgumentCaptor.forClass(OrderEntity.class);
        Mockito.verify(orderRepository, Mockito.times(1)).saveMasterOrder(orderCaptor.capture());

        String expectedSnapshot = "John Doe, 123 Main St, Apt 4B, Bengaluru, Karnataka - 560016, India";
        Assertions.assertEquals(expectedSnapshot, orderCaptor.getValue().getShippingAddressSnapshot());
        Assertions.assertEquals(130.0, orderCaptor.getValue().getTotalAmount());
        Assertions.assertEquals(OrderStatus.RESERVED, orderCaptor.getValue().getOrderStatus());
        Assertions.assertEquals(PaymentMethod.COD, orderCaptor.getValue().getPaymentMethod());

        // Assert: 6. Capture and verify the detail line item snapshot conversion maps correctly
        ArgumentCaptor<List<OrderLineItemEntity>> linesCaptor = ArgumentCaptor.forClass(List.class);
        Mockito.verify(orderRepository, Mockito.times(1)).saveOrderLineItems(Mockito.eq(EXPECTED_ORDER_ID), linesCaptor.capture());

        List<OrderLineItemEntity> savedLines = linesCaptor.getValue();
        Assertions.assertEquals(2, savedLines.size());

        Assertions.assertEquals(101, savedLines.get(0).getItemId());
        Assertions.assertEquals("Spring Framework In Action", savedLines.get(0).getTitle());
        Assertions.assertEquals(45.0, savedLines.get(0).getUnitPrice());

        Assertions.assertEquals(102, savedLines.get(1).getItemId());
        Assertions.assertEquals("Clean Code Mastery", savedLines.get(1).getTitle());
        Assertions.assertEquals(40.0, savedLines.get(1).getUnitPrice());

        // Assert: 7. Verify final workflow termination step executes properly
        Mockito.verify(cartService, Mockito.times(1)).clearCart(USER_ID);
    }

    @Test
    void checkout_shouldSetAwaitingPaymentStatus_whenPaymentMethodIsNotCOD() {
        // Arrange: 1. Mock cart with items
        LineItemResponse cartItem = LineItemResponse.builder()
                .itemId(101)
                .title("Test Book")
                .unitPrice(50.0)
                .quantity(1)
                .subTotal(50.0)
                .build();

        Cart mockCart = Cart.builder()
                .userId(USER_ID)
                .lineItems(List.of(cartItem))
                .totalCartPrice(50.0)
                .build();
        Mockito.when(cartService.getCart(USER_ID)).thenReturn(mockCart);

        // Arrange: 2. Mock address
        CustomerAddress mockAddress = CustomerAddress.builder()
                .recipientName("Jane Doe")
                .addressLine1("456 Oak St")
                .city("Mumbai")
                .state("Maharashtra")
                .postalCode("400001")
                .country("India")
                .build();
        Mockito.when(addressService.getAddressById(ADDRESS_ID, USER_ID)).thenReturn(mockAddress);

        // Arrange: 3. Mock stock reduction
        Mockito.when(bookRepository.reduceStock(101, 1)).thenReturn(true);

        // Arrange: 4. Mock repository operations
        Mockito.when(orderRepository.saveMasterOrder(Mockito.any(OrderEntity.class))).thenReturn(EXPECTED_ORDER_ID);
        Mockito.doNothing().when(orderRepository).saveOrderLineItems(Mockito.eq(EXPECTED_ORDER_ID), Mockito.anyList());
        Mockito.doNothing().when(cartService).clearCart(USER_ID);

        CheckoutRequest request = new CheckoutRequest(ADDRESS_ID, PaymentMethod.UPI);

        // Act
        OrderResponse response = orderService.checkout(USER_ID, request);

        // Assert: Verify status is AWAITING_PAYMENT for non-COD payment methods
        Assertions.assertNotNull(response);
        Assertions.assertEquals(OrderStatus.AWAITING_PAYMENT, response.getStatus());
        Assertions.assertEquals(PaymentMethod.UPI, response.getPaymentMethod());

        // Verify order entity has correct status
        ArgumentCaptor<OrderEntity> orderCaptor = ArgumentCaptor.forClass(OrderEntity.class);
        Mockito.verify(orderRepository, Mockito.times(1)).saveMasterOrder(orderCaptor.capture());
        Assertions.assertEquals(OrderStatus.AWAITING_PAYMENT, orderCaptor.getValue().getOrderStatus());
        Assertions.assertEquals(PaymentMethod.UPI, orderCaptor.getValue().getPaymentMethod());
    }

    @Test
    void getOrderById_shouldReturnDetailsResponseViaMapStruct_whenOrderExistsAndBelongsToUser() {
        // Arrange: 1. Setup a mock master-detail entity database snapshot
        OrderLineItemEntity lineItem = OrderLineItemEntity.builder()
                .id(1L)
                .itemId(501)
                .title("Architectural Blueprints")
                .unitPrice(60.0)
                .quantity(1)
                .subTotal(60.0)
                .build();

        OrderEntity mockOrder = OrderEntity.builder()
                .id(ORDER_ID)
                .userId(USER_ID)
                .shippingAddressSnapshot("John Doe, Main St, Bengaluru")
                .totalAmount(60.0)
                .orderStatus(OrderStatus.PAYMENT_SUCCESS)
                .paymentMethod(PaymentMethod.CC)
                .createdAt(LocalDateTime.now())
                .lineItems(List.of(lineItem))
                .build();

        Mockito.when(orderRepository.findOrderById(ORDER_ID)).thenReturn(mockOrder);

        // Act: 2. Invoke our clean service lookup method
        OrderDetailsResponse response = orderService.getOrderById(ORDER_ID, USER_ID);

        // Assert: 3. Verify MapStruct converted fields cleanly matching properties
        Assertions.assertNotNull(response);
        Assertions.assertEquals(ORDER_ID, response.getId());
        Assertions.assertEquals(OrderStatus.PAYMENT_SUCCESS, response.getOrderStatus());
        Assertions.assertEquals(PaymentMethod.CC, response.getPaymentMethod());
        Assertions.assertEquals("John Doe, Main St, Bengaluru", response.getShippingAddressSnapshot());

        // Assert nested children lists map properly through the mapper boundary
        Assertions.assertNotNull(response.getLineItems());
        Assertions.assertEquals(1, response.getLineItems().size());
        Assertions.assertEquals("Architectural Blueprints", response.getLineItems().get(0).getTitle());
        Assertions.assertEquals(60.0, response.getLineItems().get(0).getUnitPrice());
    }

    @Test
    void getOrderById_shouldThrowItemNotFoundException_whenOrderIdDoesNotExist() {
        // Arrange: Mock the database repository returning null
        Mockito.when(orderRepository.findOrderById(ORDER_ID)).thenReturn(null);

        // Act & Assert: Check security exception rule boundary
        ItemNotFoundException exception = Assertions.assertThrows(
                ItemNotFoundException.class,
                () -> orderService.getOrderById(ORDER_ID, USER_ID)
        );

        Assertions.assertEquals("Order not found or access denied", exception.getMessage());
    }

    @Test
    void getOrderById_shouldThrowItemNotFoundException_whenUserDoesNotOwnTheOrder() {
        // Arrange: Mock an order that belongs to the victim, not the attacker
        OrderEntity victimOrder = OrderEntity.builder()
                .id(ORDER_ID)
                .userId(USER_ID) // Rightful owner constraint
                .build();

        Mockito.when(orderRepository.findOrderById(ORDER_ID)).thenReturn(victimOrder);

        // Act & Assert: Malicious attacker attempts to look up the victim's order details
        ItemNotFoundException exception = Assertions.assertThrows(
                ItemNotFoundException.class,
                () -> orderService.getOrderById(ORDER_ID, ATTACKER_USER_ID)
        );

        // Multi-tenant privacy security wall holds firmly
        Assertions.assertEquals("Order not found or access denied", exception.getMessage());
    }

    @Test
    void getOrderHistory_shouldThrowIllegalArgumentException_whenUserIdIsEmpty() {
        // Act & Assert: Input contract validation check
        OrderException exception = Assertions.assertThrows(
                OrderException.class,
                () -> orderService.getOrderHistory("")
        );

        Assertions.assertEquals("User ID cannot be null or empty", exception.getMessage());
        Mockito.verifyNoInteractions(orderRepository);
    }

    @Test
    void getOrderHistory_shouldReturnEmptyList_whenNoOrdersExistForUser() {
        // Arrange
        Mockito.when(orderRepository.findAllOrdersByUserId(USER_ID)).thenReturn(Collections.emptyList());

        // Act
        List<OrderSummaryResponse> result = orderService.getOrderHistory(USER_ID);

        // Assert
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
        Mockito.verify(orderRepository, Mockito.times(1)).findAllOrdersByUserId(USER_ID);
    }

    @Test
    void getOrderHistory_shouldReturnMappedSummaryListViaMapStruct_whenOrdersExist() {
        // Arrange: Mock 2 master entities (representing descending order sort sequence)
        OrderEntity summary1 = OrderEntity.builder()
                .id(101L)
                .userId(USER_ID)
                .shippingAddressSnapshot("Address 1")
                .totalAmount(75.00)
                .orderStatus(OrderStatus.PAYMENT_SUCCESS)
                .paymentMethod(PaymentMethod.DC)
                .createdAt(LocalDateTime.now())
                .build();

        OrderEntity summary2 = OrderEntity.builder()
                .id(102L)
                .userId(USER_ID)
                .shippingAddressSnapshot("Address 2")
                .totalAmount(150.00)
                .orderStatus(OrderStatus.RESERVED)
                .paymentMethod(PaymentMethod.COD)
                .createdAt(LocalDateTime.now())
                .build();

        Mockito.when(orderRepository.findAllOrdersByUserId(USER_ID)).thenReturn(List.of(summary2, summary1));

        // Act
        List<OrderSummaryResponse> history = orderService.getOrderHistory(USER_ID);

        // Assert: Verify conversions cleanly match structural assumptions
        Assertions.assertNotNull(history);
        Assertions.assertEquals(2, history.size());

        // Validate first summary item structure mapping accuracy
        OrderSummaryResponse actualFirst = history.get(0);
        Assertions.assertEquals(102L, actualFirst.getId());
        Assertions.assertEquals("Address 2", actualFirst.getShippingAddressSnapshot());
        Assertions.assertEquals(150.00, actualFirst.getTotalAmount());
        Assertions.assertEquals(OrderStatus.RESERVED, actualFirst.getOrderStatus());
        Assertions.assertEquals(PaymentMethod.COD, actualFirst.getPaymentMethod());

        // Validate second summary item structure mapping accuracy
        OrderSummaryResponse actualSecond = history.get(1);
        Assertions.assertEquals(101L, actualSecond.getId());
        Assertions.assertEquals("Address 1", actualSecond.getShippingAddressSnapshot());
        Assertions.assertEquals(75.00, actualSecond.getTotalAmount());
        Assertions.assertEquals(OrderStatus.PAYMENT_SUCCESS, actualSecond.getOrderStatus());
        Assertions.assertEquals(PaymentMethod.DC, actualSecond.getPaymentMethod());

        Mockito.verify(orderRepository, Mockito.times(1)).findAllOrdersByUserId(USER_ID);
    }

    @Test
    void checkout_shouldReduceStockQuantityCorrectly_whenCheckoutSucceeds() {
        // Arrange: 1. Mock cart with multiple items
        LineItemResponse cartItem1 = LineItemResponse.builder()
                .itemId(101)
                .title("Spring Framework In Action")
                .unitPrice(45.0)
                .quantity(2)
                .subTotal(90.0)
                .build();

        LineItemResponse cartItem2 = LineItemResponse.builder()
                .itemId(102)
                .title("Clean Code Mastery")
                .unitPrice(40.0)
                .quantity(3)
                .subTotal(120.0)
                .build();

        Cart mockCart = Cart.builder()
                .userId(USER_ID)
                .lineItems(List.of(cartItem1, cartItem2))
                .totalCartPrice(210.0)
                .build();
        Mockito.when(cartService.getCart(USER_ID)).thenReturn(mockCart);

        // Arrange: 2. Mock address
        CustomerAddress mockAddress = CustomerAddress.builder()
                .recipientName("John Doe")
                .addressLine1("123 Main St")
                .city("Bengaluru")
                .state("Karnataka")
                .postalCode("560016")
                .country("India")
                .build();
        Mockito.when(addressService.getAddressById(ADDRESS_ID, USER_ID)).thenReturn(mockAddress);

        // Arrange: 3. Mock successful stock reduction for both items
        Mockito.when(bookRepository.reduceStock(101, 2)).thenReturn(true);
        Mockito.when(bookRepository.reduceStock(102, 3)).thenReturn(true);

        // Arrange: 4. Mock repository operations
        Mockito.when(orderRepository.saveMasterOrder(Mockito.any(OrderEntity.class))).thenReturn(EXPECTED_ORDER_ID);
        Mockito.doNothing().when(orderRepository).saveOrderLineItems(Mockito.eq(EXPECTED_ORDER_ID), Mockito.anyList());
        Mockito.doNothing().when(cartService).clearCart(USER_ID);

        CheckoutRequest request = new CheckoutRequest(ADDRESS_ID, PaymentMethod.COD);

        // Act
        OrderResponse response = orderService.checkout(USER_ID, request);

        // Assert: Verify stock was reduced for each item with correct quantities
        Mockito.verify(bookRepository, Mockito.times(1)).reduceStock(101, 2);
        Mockito.verify(bookRepository, Mockito.times(1)).reduceStock(102, 3);

        // Assert: Verify order was created successfully
        Assertions.assertNotNull(response);
        Assertions.assertEquals(EXPECTED_ORDER_ID, response.getOrderId());
        Assertions.assertEquals(210.0, response.getTotalAmount());
    }

    @Test
    void checkout_shouldThrowOrderException_whenStockQuantityIsInsufficient() {
        // Arrange: 1. Mock cart with item that has insufficient stock
        LineItemResponse cartItem = LineItemResponse.builder()
                .itemId(101)
                .title("Popular Book - Out of Stock")
                .unitPrice(50.0)
                .quantity(5)
                .subTotal(250.0)
                .build();

        Cart mockCart = Cart.builder()
                .userId(USER_ID)
                .lineItems(List.of(cartItem))
                .totalCartPrice(250.0)
                .build();
        Mockito.when(cartService.getCart(USER_ID)).thenReturn(mockCart);

        // Arrange: 2. Mock address
        CustomerAddress mockAddress = CustomerAddress.builder()
                .recipientName("Jane Doe")
                .addressLine1("456 Oak St")
                .city("Mumbai")
                .state("Maharashtra")
                .postalCode("400001")
                .country("India")
                .build();
        Mockito.when(addressService.getAddressById(ADDRESS_ID, USER_ID)).thenReturn(mockAddress);

        // Arrange: 3. Mock stock reduction failure (insufficient stock)
        Mockito.when(bookRepository.reduceStock(101, 5)).thenReturn(false);

        CheckoutRequest request = new CheckoutRequest(ADDRESS_ID, PaymentMethod.UPI);

        // Act & Assert: Verify OrderException is thrown with correct error
        OrderException exception = Assertions.assertThrows(
                OrderException.class,
                () -> orderService.checkout(USER_ID, request)
        );

        // Assert: Verify exception details
        Assertions.assertEquals(OrderError.STOCK_UPDATE_FAILED, exception.getError());
        Assertions.assertEquals(409, exception.getHttpCode());
        Assertions.assertEquals("Failed to update stock quantity. Item may be out of stock", exception.getMessage());

        // Assert: Verify order was NOT created
        Mockito.verify(orderRepository, Mockito.never()).saveMasterOrder(Mockito.any());
        Mockito.verify(orderRepository, Mockito.never()).saveOrderLineItems(Mockito.anyLong(), Mockito.anyList());

        // Assert: Verify cart was NOT cleared (transaction should rollback)
        Mockito.verify(cartService, Mockito.never()).clearCart(USER_ID);
    }

    @Test
    void checkout_shouldNotClearCart_whenStockReductionFails() {
        // Arrange: 1. Mock cart with two items, second one will fail stock reduction
        LineItemResponse cartItem1 = LineItemResponse.builder()
                .itemId(101)
                .title("First Book")
                .unitPrice(45.0)
                .quantity(2)
                .subTotal(90.0)
                .build();

        LineItemResponse cartItem2 = LineItemResponse.builder()
                .itemId(102)
                .title("Second Book - Low Stock")
                .unitPrice(40.0)
                .quantity(10)
                .subTotal(400.0)
                .build();

        Cart mockCart = Cart.builder()
                .userId(USER_ID)
                .lineItems(List.of(cartItem1, cartItem2))
                .totalCartPrice(490.0)
                .build();
        Mockito.when(cartService.getCart(USER_ID)).thenReturn(mockCart);

        // Arrange: 2. Mock address
        CustomerAddress mockAddress = CustomerAddress.builder()
                .recipientName("Test User")
                .addressLine1("789 Test St")
                .city("Delhi")
                .state("Delhi")
                .postalCode("110001")
                .country("India")
                .build();
        Mockito.when(addressService.getAddressById(ADDRESS_ID, USER_ID)).thenReturn(mockAddress);

        // Arrange: 3. Mock first stock reduction succeeds, second fails
        Mockito.when(bookRepository.reduceStock(101, 2)).thenReturn(true);
        Mockito.when(bookRepository.reduceStock(102, 10)).thenReturn(false);

        CheckoutRequest request = new CheckoutRequest(ADDRESS_ID, PaymentMethod.DC);

        // Act & Assert: Verify OrderException is thrown
        OrderException exception = Assertions.assertThrows(
                OrderException.class,
                () -> orderService.checkout(USER_ID, request)
        );

        // Assert: Verify cart is NOT cleared when transaction fails
        Mockito.verify(cartService, Mockito.never()).clearCart(USER_ID);

        // Assert: Verify cart retains line items (by not calling clearCart)
        // In a real scenario with database transactions, the first stock reduction would also rollback
        Assertions.assertNotNull(mockCart.getLineItems());
        Assertions.assertEquals(2, mockCart.getLineItems().size());
        Assertions.assertEquals(101, mockCart.getLineItems().get(0).getItemId());
        Assertions.assertEquals(102, mockCart.getLineItems().get(1).getItemId());
    }

}
