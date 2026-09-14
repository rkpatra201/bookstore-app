package com.bookstore.backend.repositories;

import com.bookstore.backend.entities.OrderEntity;
import com.bookstore.backend.entities.OrderLineItemEntity;
import com.bookstore.backend.enums.OrderStatus;
import com.bookstore.backend.enums.PaymentMethod;
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
                INSERT INTO orders (user_id, shipping_address_snapshot, total_amount, order_status, payment_method)
                VALUES (?, ?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, order.getUserId());
            ps.setString(2, order.getShippingAddressSnapshot());
            ps.setDouble(3, order.getTotalAmount());
            ps.setString(4, order.getOrderStatus() != null ? order.getOrderStatus().name() : OrderStatus.RESERVED.name());
            ps.setString(5, order.getPaymentMethod().name());
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

    /**
     * Finds a single order along with all its nested line items by its ID.
     * Returns null if no matching order record is found.
     */
    public OrderEntity findOrderById(Long orderId) {
        String orderSql = """
                SELECT id, user_id, shipping_address_snapshot, total_amount, order_status, payment_method, created_at, updated_at
                FROM orders
                WHERE id = ?
                """;

        // 1. Fetch the master order row mapping
        List<OrderEntity> orders = jdbcTemplate.query(orderSql, (rs, rowNum) -> {
            OrderEntity order = new OrderEntity();
            order.setId(rs.getLong("id"));
            order.setUserId(rs.getString("user_id"));
            order.setShippingAddressSnapshot(rs.getString("shipping_address_snapshot"));
            order.setTotalAmount(rs.getDouble("total_amount"));
            order.setOrderStatus(OrderStatus.valueOf(rs.getString("order_status")));
            order.setPaymentMethod(PaymentMethod.valueOf(rs.getString("payment_method")));
            order.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            order.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
            return order;
        }, orderId);

        if (orders.isEmpty()) {
            return null;
        }

        OrderEntity masterOrder = orders.get(0);

        // 2. Fetch all matching line items attached to this specific order ID
        String lineItemsSql = """
                SELECT id, order_id, item_id, title, unit_price, quantity, sub_total
                FROM order_line_items 
                WHERE order_id = ?
                ORDER BY id ASC
                """;

        List<OrderLineItemEntity> items = jdbcTemplate.query(lineItemsSql, (rs, rowNum) -> {
            OrderLineItemEntity item = new OrderLineItemEntity();
            item.setId(rs.getLong("id"));
            item.setOrderId(rs.getLong("order_id"));
            item.setItemId(rs.getInt("item_id"));
            item.setTitle(rs.getString("title"));
            item.setUnitPrice(rs.getDouble("unit_price"));
            item.setQuantity(rs.getInt("quantity"));
            item.setSubTotal(rs.getDouble("sub_total"));
            return item;
        }, orderId);

        // 3. Embed the child details collection into the master entity object safely
        masterOrder.setLineItems(items);

        return masterOrder;
    }

    /**
     * Retrieves all summary order records for a specific user ID.
     * Excludes detailed line item collections to optimize list rendering.
     */
    public List<OrderEntity> findAllOrdersByUserId(String userId) {
        String sql = """
                SELECT id, user_id, shipping_address_snapshot, total_amount, order_status, payment_method, created_at, updated_at
                FROM orders
                WHERE user_id = ?
                ORDER BY created_at DESC
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            OrderEntity order = new OrderEntity();
            order.setId(rs.getLong("id"));
            order.setUserId(rs.getString("user_id"));
            order.setShippingAddressSnapshot(rs.getString("shipping_address_snapshot"));
            order.setTotalAmount(rs.getDouble("total_amount"));
            order.setOrderStatus(OrderStatus.valueOf(rs.getString("order_status")));
            order.setPaymentMethod(PaymentMethod.valueOf(rs.getString("payment_method")));
            order.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            order.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
            // lineItems list remains null or uninitialized on purpose for summary listings
            return order;
        }, userId);
    }


}
