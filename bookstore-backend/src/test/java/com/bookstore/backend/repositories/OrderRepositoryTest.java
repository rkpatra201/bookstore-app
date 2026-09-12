package com.bookstore.backend.repositories;

import com.bookstore.backend.entities.OrderEntity;
import com.bookstore.backend.entities.OrderLineItemEntity;
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

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final String USER_ID = "customer-user-111";
    private static final String ADDRESS_SNAPSHOT = "Flat 402, Green Glen Layout, Bengaluru, Karnataka, 560103";

    @Test
    void saveMasterOrderAndLineItems_shouldPersistCorrectlyInDatabase() {
        // Arrange: 1. Build the master order entity
        OrderEntity order = OrderEntity.builder()
                .userId(USER_ID)
                .shippingAddressSnapshot(ADDRESS_SNAPSHOT)
                .totalAmount(130.00)
                .orderStatus("PENDING")
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
        Assertions.assertEquals("PENDING", dbOrder.get("order_status"));

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
}
