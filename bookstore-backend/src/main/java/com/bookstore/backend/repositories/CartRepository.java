package com.bookstore.backend.repositories;

import com.bookstore.backend.entities.CartLineItemEntity;
import com.bookstore.backend.enums.CartError;
import com.bookstore.backend.exceptions.CartException;
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
     * Uses MERGE syntax which is compatible with both H2 and MySQL 8.0+.
     */
    public void saveOrUpdate(CartLineItemEntity lineItem) {
        // Use MERGE statement which is supported by both H2 and MySQL 8.0+
        String sql = """
                MERGE INTO cart_line_items (user_id, item_id, quantity)
                KEY (user_id, item_id)
                VALUES (?, ?, ?)
                """;

        // First, try to get existing quantity
        String selectSql = """
                SELECT quantity FROM cart_line_items
                WHERE user_id = ? AND item_id = ?
                """;

        try {
            Integer existingQuantity = jdbcTemplate.queryForObject(selectSql,
                    Integer.class,
                    lineItem.getUserId(),
                    lineItem.getItemId());

            // Update: add to existing quantity
            String updateSql = """
                    UPDATE cart_line_items
                    SET quantity = quantity + ?
                    WHERE user_id = ? AND item_id = ?
                    """;

            jdbcTemplate.update(updateSql,
                    lineItem.getQuantity(),
                    lineItem.getUserId(),
                    lineItem.getItemId());

        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            // Insert: item doesn't exist yet
            String insertSql = """
                    INSERT INTO cart_line_items (user_id, item_id, quantity)
                    VALUES (?, ?, ?)
                    """;

            jdbcTemplate.update(insertSql,
                    lineItem.getUserId(),
                    lineItem.getItemId(),
                    lineItem.getQuantity());
        }
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

    public void reduceItemCount(String userId, int itemId, int quantityToReduce) {
        String updateSql = """
                UPDATE cart_line_items
                SET quantity = quantity - ?
                WHERE user_id = ? AND item_id = ?
                """;

        int rowsAffected = jdbcTemplate.update(updateSql, quantityToReduce, userId, itemId);

        if (rowsAffected == 0) {
            throw new CartException(CartError.ITEM_NOT_FOUND);
        }

        String deleteSql = """
                DELETE FROM cart_line_items
                WHERE user_id = ? AND item_id = ? AND quantity <= 0
                """;

        jdbcTemplate.update(deleteSql, userId, itemId);
    }

    /**
     * Completely empties the shopping cart for a specific user ID.
     */
    public void clearCart(String userId) {
        String sql = """
                DELETE FROM cart_line_items 
                WHERE user_id = ?
                """;

        jdbcTemplate.update(sql, userId);
    }

}
