package com.bookstore.backend.repositories;

import com.bookstore.backend.entities.OrderEntity;
import com.bookstore.backend.entities.OrderLineItemEntity;
import com.bookstore.backend.enums.OrderStatus;
import com.bookstore.backend.enums.PaymentMethod;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

@JdbcTest
@ActiveProfiles("test")
@Import(OrderRepository.class)
class OrderRepositoryTest {

    private static final String USER_ID = "customer-user-111";
    private static final String ADDRESS_SNAPSHOT = "Flat 402, Green Glen Layout, Bengaluru, Karnataka, 560103";
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void saveMasterOrderAndLineItems_shouldPersistCorrectlyInDatabase() {
        // Arrange: 1. Build the master order entity
        OrderEntity order = OrderEntity.builder()
                .userId(USER_ID)
                .shippingAddressSnapshot(ADDRESS_SNAPSHOT)
                .totalAmount(130.00)
                .orderStatus(OrderStatus.AWAITING_PAYMENT)
                .paymentMethod(PaymentMethod.UPI)
                .build();

        // Act: 2. Save the master order record and retrieve the generated key ID
        Long generatedOrderId = orderRepository.saveMasterOrder(order);

        // Assert: Confirm the primary key ID was generated successfully
        Assertions.assertNotNull(generatedOrderId);
        Assertions.assertTrue(generatedOrderId > 0);

        // Arrange: 3. Create detail order line items using the generated key ID
        OrderLineItemEntity item1 = OrderLineItemEntity.builder()
                .itemId(101)
                .title("Spring Microservices")
                .unitPrice(40.00)
                .quantity(2)
                .subTotal(80.00)
                .build();

        OrderLineItemEntity item2 = OrderLineItemEntity.builder()
                .itemId(102)
                .title("Effective Architecture")
                .unitPrice(50.00)
                .quantity(1)
                .subTotal(50.00)
                .build();

        List<OrderLineItemEntity> lineItems = List.of(item1, item2);

        // Act: 4. Perform the batch database line insertion
        orderRepository.saveOrderLineItems(generatedOrderId, lineItems);

        // --- Assertions via Direct Table Queries ---

        // Check Master Orders table row values
        Map<String, Object> dbOrder = jdbcTemplate.queryForMap(
                "SELECT * FROM orders WHERE id = ?", generatedOrderId
        );
        Assertions.assertEquals(USER_ID, dbOrder.get("user_id"));
        Assertions.assertEquals(ADDRESS_SNAPSHOT, dbOrder.get("shipping_address_snapshot"));
        Assertions.assertEquals(130.00, ((Number) dbOrder.get("total_amount")).doubleValue());
        Assertions.assertEquals("AWAITING_PAYMENT", dbOrder.get("order_status"));
        Assertions.assertEquals("UPI", dbOrder.get("payment_method"));

        // Check Order Line Items table rows size and positional parameters
        List<Map<String, Object>> dbLineItems = jdbcTemplate.queryForList(
                "SELECT * FROM order_line_items WHERE order_id = ? ORDER BY item_id ASC", generatedOrderId
        );
        Assertions.assertEquals(2, dbLineItems.size());

        // Verify First Row Values
        Map<String, Object> actualItem1 = dbLineItems.get(0);
        Assertions.assertEquals(101, dbLineItems.get(0).get("item_id"));
        Assertions.assertEquals("Spring Microservices", actualItem1.get("title"));
        Assertions.assertEquals(40.00, ((Number) actualItem1.get("unit_price")).doubleValue());
        Assertions.assertEquals(2, actualItem1.get("quantity"));
        Assertions.assertEquals(80.00, ((Number) actualItem1.get("sub_total")).doubleValue());

        // Verify Second Row Values
        Map<String, Object> actualItem2 = dbLineItems.get(1);
        Assertions.assertEquals(102, dbLineItems.get(1).get("item_id"));
        Assertions.assertEquals("Effective Architecture", actualItem2.get("title"));
        Assertions.assertEquals(50.00, ((Number) actualItem2.get("unit_price")).doubleValue());
        Assertions.assertEquals(1, actualItem2.get("quantity"));
        Assertions.assertEquals(50.00, ((Number) actualItem2.get("sub_total")).doubleValue());
    }

    @Test
    void findOrderById_shouldReturnFullOrderDetailsWithLineItems_whenOrderExists() {
        // Arrange: 1. Persist a master order record
        OrderEntity newOrder = OrderEntity.builder()
                .userId("customer-user-111")
                .shippingAddressSnapshot("John Doe, 123 Main St, Bengaluru")
                .totalAmount(95.00)
                .orderStatus(OrderStatus.RESERVED)
                .paymentMethod(PaymentMethod.COD)
                .build();

        Long generatedOrderId = orderRepository.saveMasterOrder(newOrder);

        // Arrange: 2. Persist detail child item snapshot records attached to the generated ID
        OrderLineItemEntity lineItem1 = OrderLineItemEntity.builder()
                .itemId(101)
                .title("Spring Boot Action")
                .unitPrice(45.00)
                .quantity(1)
                .subTotal(45.00)
                .build();

        OrderLineItemEntity lineItem2 = OrderLineItemEntity.builder()
                .itemId(102)
                .title("Clean Architecture")
                .unitPrice(50.00)
                .quantity(1)
                .subTotal(50.00)
                .build();

        orderRepository.saveOrderLineItems(generatedOrderId, List.of(lineItem1, lineItem2));

        // Act: 3. Query the database using our new deep lookup operation method
        OrderEntity retrievedOrder = orderRepository.findOrderById(generatedOrderId);

        // Assert: 4. Check master order root fields properties
        Assertions.assertNotNull(retrievedOrder);
        Assertions.assertEquals(generatedOrderId, retrievedOrder.getId());
        Assertions.assertEquals("customer-user-111", retrievedOrder.getUserId());
        Assertions.assertEquals("John Doe, 123 Main St, Bengaluru", retrievedOrder.getShippingAddressSnapshot());
        Assertions.assertEquals(95.00, retrievedOrder.getTotalAmount());
        Assertions.assertEquals(OrderStatus.RESERVED, retrievedOrder.getOrderStatus());
        Assertions.assertEquals(PaymentMethod.COD, retrievedOrder.getPaymentMethod());
        Assertions.assertNotNull(retrievedOrder.getCreatedAt());

        // Assert: 5. Verify the embedded collections size and content alignment
        List<OrderLineItemEntity> items = retrievedOrder.getLineItems();
        Assertions.assertNotNull(items);
        Assertions.assertEquals(2, items.size());

        // Check line item 1 values
        Assertions.assertEquals(101, items.get(0).getItemId());
        Assertions.assertEquals("Spring Boot Action", items.get(0).getTitle());
        Assertions.assertEquals(45.00, items.get(0).getUnitPrice());

        // Check line item 2 values
        Assertions.assertEquals(102, items.get(1).getItemId());
        Assertions.assertEquals("Clean Architecture", items.get(1).getTitle());
        Assertions.assertEquals(50.00, items.get(1).getUnitPrice());
    }

    @Test
    void findOrderById_shouldReturnNull_whenOrderDoesNotExist() {
        // Act: Querying a completely non-existent order index sequence reference
        OrderEntity nonExistentOrder = orderRepository.findOrderById(99999L);

        // Assert
        Assertions.assertNull(nonExistentOrder);
    }

    @Test
    void findAllOrdersByUserId_shouldReturnSummaryListInDescendingOrder_whenOrdersExist() throws InterruptedException {
        String targetUserId = "customer-user-111";
        String otherUserId = "noisy-neighbor-999";

        // Arrange: Seed 2 separate orders for our target user
        OrderEntity order1 = OrderEntity.builder()
                .userId(targetUserId)
                .shippingAddressSnapshot("Address 1")
                .totalAmount(50.00)
                .orderStatus(OrderStatus.PAYMENT_SUCCESS)
                .paymentMethod(PaymentMethod.CC)
                .build();

        OrderEntity order2 = OrderEntity.builder()
                .userId(targetUserId)
                .shippingAddressSnapshot("Address 2")
                .totalAmount(120.00)
                .orderStatus(OrderStatus.AWAITING_PAYMENT)
                .paymentMethod(PaymentMethod.BANK)
                .build();

        // Arrange: Seed an order for a different customer to verify multi-tenant query isolation bounds
        OrderEntity noiseOrder = OrderEntity.builder()
                .userId(otherUserId)
                .shippingAddressSnapshot("Address 3")
                .totalAmount(99.00)
                .orderStatus(OrderStatus.RESERVED)
                .paymentMethod(PaymentMethod.COD)
                .build();

        // Persist all entries sequentially
        orderRepository.saveMasterOrder(order1);
        // Small sleep or structural gap to ensure distinct auto-generated created_at timestamp sequences
        Thread.sleep(1000);
        orderRepository.saveMasterOrder(order2);
        orderRepository.saveMasterOrder(noiseOrder);

        // Act: Execute the query
        List<OrderEntity> historicalSummaries = orderRepository.findAllOrdersByUserId(targetUserId);

        // Assert: Verify size and check that noise values are excluded
        Assertions.assertNotNull(historicalSummaries);
        Assertions.assertEquals(2, historicalSummaries.size());

        // Assert Descending Order Precedence Check: order2 must be element 0 (newest first)
        Assertions.assertEquals(120.00, historicalSummaries.get(0).getTotalAmount());
        Assertions.assertEquals(OrderStatus.AWAITING_PAYMENT, historicalSummaries.get(0).getOrderStatus());
        Assertions.assertEquals(PaymentMethod.BANK, historicalSummaries.get(0).getPaymentMethod());
        Assertions.assertNull(historicalSummaries.get(0).getLineItems()); // Line items must be omitted

        // Check older entry
        Assertions.assertEquals(50.00, historicalSummaries.get(1).getTotalAmount());
        Assertions.assertEquals(OrderStatus.PAYMENT_SUCCESS, historicalSummaries.get(1).getOrderStatus());
        Assertions.assertEquals(PaymentMethod.CC, historicalSummaries.get(1).getPaymentMethod());
    }

    @Test
    void findAllOrdersByUserId_shouldReturnEmptyList_whenUserHasNoOrderHistory() {
        // Act
        List<OrderEntity> emptyHistory = orderRepository.findAllOrdersByUserId("new-user-with-zero-orders");

        // Assert
        Assertions.assertNotNull(emptyHistory);
        Assertions.assertTrue(emptyHistory.isEmpty());
    }


}
