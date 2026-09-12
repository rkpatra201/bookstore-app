package com.bookstore.backend.repositories;

import com.bookstore.backend.entities.CartLineItemEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CartRepository {

    private final JdbcTemplate jdbcTemplate;

    public CartRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Removes a specific item from a user's cart completely.
     */
    public boolean deleteItem(String userId, int itemId) {
        String sql = """
                DELETE FROM cart_line_items 
                WHERE user_id = ? AND item_id = ?
                """;

        int rowsAffected = jdbcTemplate.update(sql, userId, itemId);
        return rowsAffected > 0; // Returns true if an item was actually found and deleted
    }

    /**
     * Saves a line item to the cart. 
     * If the user already has this item, it adds the new quantity to the existing quantity.
     */
    public void saveOrUpdate(CartLineItemEntity lineItem) {
        String sql = """
                INSERT INTO cart_line_items (user_id, item_id, quantity)
                VALUES (?, ?, ?)
                ON DUPLICATE KEY UPDATE quantity = quantity + VALUES(quantity)
                """;
                
        // Note: For H2 databases during testing, use this SQL variant instead:
        // ON CONFLICT(user_id, item_id) DO UPDATE SET quantity = cart_line_items.quantity + EXCLUDED.quantity

        jdbcTemplate.update(sql, 
                lineItem.getUserId(), 
                lineItem.getItemId(), 
                lineItem.getQuantity()
        );
    }

    public List<CartLineItemEntity> findByUserId(String userId) {
        String sql = """
                SELECT id, user_id, item_id, quantity 
                FROM cart_line_items 
                WHERE user_id = ?
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            CartLineItemEntity entity = new CartLineItemEntity();
            entity.setId(rs.getInt("id"));
            entity.setUserId(rs.getString("user_id"));
            entity.setItemId(rs.getInt("item_id"));
            entity.setQuantity(rs.getInt("quantity"));
            return entity;
        }, userId);
    }
}
