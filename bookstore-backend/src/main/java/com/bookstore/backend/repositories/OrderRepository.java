package com.bookstore.backend.repositories;

import com.bookstore.backend.entities.OrderEntity;
import com.bookstore.backend.entities.OrderLineItemEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

@Repository
public class OrderRepository {

    private final JdbcTemplate jdbcTemplate;

    public OrderRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Persists the master order record and returns the generated primary key ID.
     */
    public Long saveMasterOrder(OrderEntity order) {
        String sql = """
                INSERT INTO orders (user_id, shipping_address_snapshot, total_amount, order_status)
                VALUES (?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, order.getUserId());
            ps.setString(2, order.getShippingAddressSnapshot());
            ps.setDouble(3, order.getTotalAmount());
            ps.setString(4, order.getOrderStatus() != null ? order.getOrderStatus() : "PENDING");
            return ps;
        }, keyHolder);

        // FIX: Extract specifically the "id" key from the multi-key map list returned by H2
        List<Map<String, Object>> keyList = keyHolder.getKeyList();
        if (keyList == null || keyList.isEmpty()) {
            throw new IllegalStateException("Failed to retrieve generated order ID");
        }

        // Get the first map row and target the "id" or "ID" key safely
        Map<String, Object> keys = keyList.get(0);
        Number idKey = (Number) keys.getOrDefault("id", keys.get("ID"));

        if (idKey == null) {
            throw new IllegalStateException("Primary key 'id' not found in generated keys map");
        }

        return idKey.longValue();
    }

    /**
     * Inserts the list of purchased item details attached to a specific order ID.
     */
    public void saveOrderLineItems(Long orderId, List<OrderLineItemEntity> lineItems) {
        String sql = """
                INSERT INTO order_line_items (order_id, item_id, title, unit_price, quantity, sub_total)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        // Map and batch execute all line entries simultaneously
        List<Object[]> batchArgs = lineItems.stream()
                .map(item -> new Object[]{
                        orderId,
                        item.getItemId(),
                        item.getTitle(),
                        item.getUnitPrice(),
                        item.getQuantity(),
                        item.getSubTotal()
                })
                .toList();

        jdbcTemplate.batchUpdate(sql, batchArgs);
    }
}
