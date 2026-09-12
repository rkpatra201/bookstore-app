package com.bookstore.backend.controllers;

import com.bookstore.backend.dtos.CheckoutRequest;
import com.bookstore.backend.dtos.DataResponse;
import com.bookstore.backend.dtos.OrderDetailsResponse;
import com.bookstore.backend.dtos.OrderResponse;
import com.bookstore.backend.dtos.UserContext;
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

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @Mock
    private UserContextService userContextService;

    @InjectMocks
    private OrderController orderController;

    private static final String USER_ID = "customer-user-111";
    private static final Long ADDRESS_ID = 55L;
    private static final Long ORDER_ID = 999L;

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
}
