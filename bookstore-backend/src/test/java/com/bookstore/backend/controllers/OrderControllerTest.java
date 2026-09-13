package com.bookstore.backend.controllers;

import com.bookstore.backend.dtos.*;
import com.bookstore.backend.services.OrderService;
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

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    private static final String USER_ID = "customer-user-111";
    private static final Long ADDRESS_ID = 55L;
    private static final Long ORDER_ID = 999L;
    @Mock
    private OrderService orderService;
    @Mock
    private UserContextService userContextService;
    @InjectMocks
    private OrderController orderController;

    @Test
    void checkout_shouldReturnOkAndOrderDetails_whenSuccessful() {
        // Arrange
        CheckoutRequest request = new CheckoutRequest(ADDRESS_ID);

        UserContext mockContext = Mockito.mock(UserContext.class);
        Mockito.when(mockContext.getUserId()).thenReturn(USER_ID);
        Mockito.when(userContextService.getUserContext()).thenReturn(mockContext);

        OrderResponse mockOrderResponse = new OrderResponse(ORDER_ID, "PENDING", 120.0);
        Mockito.when(orderService.checkout(USER_ID, request)).thenReturn(mockOrderResponse);

        // Act
        ResponseEntity<DataResponse<OrderResponse>> responseEntity = orderController.checkout(request);

        // Assert
        Assertions.assertNotNull(responseEntity);
        Assertions.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        DataResponse<OrderResponse> envelope = responseEntity.getBody();
        Assertions.assertNotNull(envelope);
        Assertions.assertTrue(envelope.isSuccess());
        Assertions.assertEquals("Order placed successfully", envelope.getMessage());
        Assertions.assertEquals(ORDER_ID, envelope.getData().getOrderId());

        Mockito.verify(orderService, Mockito.times(1)).checkout(USER_ID, request);
    }

    @Test
    void getOrderById_shouldReturnOkAndFullOrderDetails_whenSuccessful() {
        // Arrange
        UserContext mockContext = Mockito.mock(UserContext.class);
        Mockito.when(mockContext.getUserId()).thenReturn(USER_ID);
        Mockito.when(userContextService.getUserContext()).thenReturn(mockContext);

        OrderDetailsResponse mockDetails = OrderDetailsResponse.builder()
                .id(ORDER_ID)
                .shippingAddressSnapshot("John Doe, Main St, Bengaluru")
                .totalAmount(120.0)
                .orderStatus("PENDING")
                .createdAt(LocalDateTime.now())
                .lineItems(Collections.emptyList())
                .build();
        Mockito.when(orderService.getOrderById(ORDER_ID, USER_ID)).thenReturn(mockDetails);

        // Act
        ResponseEntity<DataResponse<OrderDetailsResponse>> responseEntity = orderController.getOrderById(ORDER_ID);

        // Assert
        Assertions.assertNotNull(responseEntity);
        Assertions.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        DataResponse<OrderDetailsResponse> envelope = responseEntity.getBody();
        Assertions.assertNotNull(envelope);
        Assertions.assertTrue(envelope.isSuccess());
        Assertions.assertEquals("Order details retrieved successfully", envelope.getMessage());
        Assertions.assertEquals(ORDER_ID, envelope.getData().getId());

        Mockito.verify(orderService, Mockito.times(1)).getOrderById(ORDER_ID, USER_ID);
    }

    @Test
    void getOrderHistory_shouldReturnOkAndSummaryList_whenSuccessful() {
        // Arrange: 1. Setup mock order summaries
        OrderSummaryResponse summary1 = OrderSummaryResponse.builder()
                .id(101L)
                .shippingAddressSnapshot("Address 1")
                .totalAmount(45.00)
                .orderStatus("DELIVERED")
                .createdAt(LocalDateTime.now())
                .build();

        OrderSummaryResponse summary2 = OrderSummaryResponse.builder()
                .id(102L)
                .shippingAddressSnapshot("Address 2")
                .totalAmount(95.00)
                .orderStatus("PENDING")
                .createdAt(LocalDateTime.now())
                .build();

        List<OrderSummaryResponse> mockHistory = List.of(summary2, summary1);

        // Arrange: 2. Mock UserContext resolution step-by-step
        UserContext mockContext = Mockito.mock(UserContext.class);
        Mockito.when(mockContext.getUserId()).thenReturn(USER_ID);
        Mockito.when(userContextService.getUserContext()).thenReturn(mockContext);

        // Arrange: 3. Mock service history lookup output return configuration
        Mockito.when(orderService.getOrderHistory(USER_ID)).thenReturn(mockHistory);

        // Act: 4. Execute the endpoint function call
        ResponseEntity<DataResponse<List<OrderSummaryResponse>>> responseEntity = orderController.getOrderHistory();

        // Assert: 5. Verify HTTP status code and structural data formats
        Assertions.assertNotNull(responseEntity);
        Assertions.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        DataResponse<List<OrderSummaryResponse>> envelope = responseEntity.getBody();
        Assertions.assertNotNull(envelope);
        Assertions.assertTrue(envelope.isSuccess());
        Assertions.assertEquals("Order history retrieved successfully", envelope.getMessage());

        // Validate collection mapping integrity parameters
        List<OrderSummaryResponse> dataList = envelope.getData();
        Assertions.assertNotNull(dataList);
        Assertions.assertEquals(2, dataList.size());
        Assertions.assertEquals(102L, dataList.get(0).getId());
        Assertions.assertEquals(95.00, dataList.get(0).getTotalAmount());
        Assertions.assertEquals(101L, dataList.get(1).getId());
        Assertions.assertEquals(45.00, dataList.get(1).getTotalAmount());

        // Verify layer communication integration borders
        Mockito.verify(userContextService, Mockito.times(1)).getUserContext();
        Mockito.verify(orderService, Mockito.times(1)).getOrderHistory(USER_ID);
    }

}
