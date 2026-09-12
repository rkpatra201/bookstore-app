package com.bookstore.backend.repositories;

import com.bookstore.backend.entities.CustomerAddressEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AddressRepository {

    private final JdbcTemplate jdbcTemplate;

    public AddressRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Shared RowMapper reusable across find and list queries
    private final RowMapper<CustomerAddressEntity> addressRowMapper = (rs, rowNum) -> {
        CustomerAddressEntity entity = new CustomerAddressEntity();
        entity.setId(rs.getLong("id"));
        entity.setUserId(rs.getString("user_id"));
        entity.setAlias(rs.getString("alias"));
        entity.setRecipientName(rs.getString("recipient_name"));
        entity.setAddressLine1(rs.getString("address_line1"));
        entity.setAddressLine2(rs.getString("address_line2"));
        entity.setCity(rs.getString("city"));
        entity.setState(rs.getString("state"));
        entity.setPostalCode(rs.getString("postal_code"));
        entity.setCountry(rs.getString("country"));
        entity.setPhoneNumber(rs.getString("phone_number"));
        entity.setDefault(rs.getBoolean("is_default"));
        entity.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return entity;
    };

    /**
     * Inserts a new customer address record.
     */
    public void add(CustomerAddressEntity address) {
        String sql = """
                INSERT INTO customer_addresses (user_id, alias, recipient_name, address_line1, address_line2, city, state, postal_code, country, phone_number, is_default)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        jdbcTemplate.update(sql,
                address.getUserId(),
                address.getAlias(),
                address.getRecipientName(),
                address.getAddressLine1(),
                address.getAddressLine2(),
                address.getCity(),
                address.getState(),
                address.getPostalCode(),
                address.getCountry(),
                address.getPhoneNumber(),
                address.isDefault()
        );
    }

    /**
     * Updates an existing address matching both the ID and owner's user ID.
     * Returns true if a row was updated.
     */
    public boolean updateByIdAndUserId(CustomerAddressEntity address) {
        String sql = """
                UPDATE customer_addresses 
                SET alias = ?, recipient_name = ?, address_line1 = ?, address_line2 = ?, city = ?, state = ?, postal_code = ?, country = ?, phone_number = ?, is_default = ?
                WHERE id = ? AND user_id = ?
                """;

        int rowsAffected = jdbcTemplate.update(sql,
                address.getAlias(),
                address.getRecipientName(),
                address.getAddressLine1(),
                address.getAddressLine2(),
                address.getCity(),
                address.getState(),
                address.getPostalCode(),
                address.getCountry(),
                address.getPhoneNumber(),
                address.isDefault(),
                address.getId(),
                address.getUserId()
        );

        return rowsAffected > 0;
    }

    /**
     * Deletes a specific address row securely.
     * Returns true if a row was matched and purged.
     */
    public boolean deleteByIdAndUserId(Long id, String userId) {
        String sql = """
                DELETE FROM customer_addresses 
                WHERE id = ? AND user_id = ?
                """;

        int rowsAffected = jdbcTemplate.update(sql, id, userId);
        return rowsAffected > 0;
    }

    /**
     * Finds a single address record by ID ensuring user context alignment.
     * Returns null if no record is found.
     */
    public CustomerAddressEntity findByIdAndUserId(Long id, String userId) {
        String sql = """
                SELECT id, user_id, alias, recipient_name, address_line1, address_line2, city, state, postal_code, country, phone_number, is_default, created_at
                FROM customer_addresses 
                WHERE id = ? AND user_id = ?
                """;

        List<CustomerAddressEntity> results = jdbcTemplate.query(sql, addressRowMapper, id, userId);
        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * Lists all saved addresses for a given user ID.
     */
    public List<CustomerAddressEntity> findAllByUserId(String userId) {
        String sql = """
                SELECT id, user_id, alias, recipient_name, address_line1, address_line2, city, state, postal_code, country, phone_number, is_default, created_at
                FROM customer_addresses 
                WHERE user_id = ?
                ORDER BY is_default DESC, created_at DESC
                """;

        return jdbcTemplate.query(sql, addressRowMapper, userId);
    }
}
