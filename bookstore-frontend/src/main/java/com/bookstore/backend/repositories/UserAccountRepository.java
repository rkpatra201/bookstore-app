package com.bookstore.backend.repositories;

import com.bookstore.backend.entities.UserAccountEntity;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserAccountRepository {

    private final JdbcTemplate jdbcTemplate;

    public UserAccountRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public UserAccountEntity saveUserAccount(UserAccountEntity userAccount) {
        String sql = """
                INSERT INTO user_account (user_id, email, first_name, last_name, password, blocked)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        jdbcTemplate.update(sql,
                userAccount.getUserId(),
                userAccount.getEmail(),
                userAccount.getFirstName(),
                userAccount.getLastName(),
                userAccount.getPassword(),
                userAccount.getBlocked() != null ? userAccount.getBlocked() : false
        );

        return findByUserId(userAccount.getUserId()).orElse(userAccount);
    }

    public Optional<UserAccountEntity> findByUserId(String userId) {
        String sql = """
                SELECT user_id, email, first_name, last_name, password, blocked, created_at, updated_at
                FROM user_account
                WHERE user_id = ?
                """;

        try {
            UserAccountEntity userAccount = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                UserAccountEntity entity = new UserAccountEntity();
                entity.setUserId(rs.getString("user_id"));
                entity.setEmail(rs.getString("email"));
                entity.setFirstName(rs.getString("first_name"));
                entity.setLastName(rs.getString("last_name"));
                entity.setPassword(rs.getString("password"));
                entity.setBlocked(rs.getBoolean("blocked"));
                entity.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                entity.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                return entity;
            }, userId);

            return Optional.of(userAccount);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<UserAccountEntity> findByEmail(String email) {
        String sql = """
                SELECT user_id, email, first_name, last_name, password, blocked, created_at, updated_at
                FROM user_account
                WHERE email = ?
                """;

        try {
            UserAccountEntity userAccount = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                UserAccountEntity entity = new UserAccountEntity();
                entity.setUserId(rs.getString("user_id"));
                entity.setEmail(rs.getString("email"));
                entity.setFirstName(rs.getString("first_name"));
                entity.setLastName(rs.getString("last_name"));
                entity.setPassword(rs.getString("password"));
                entity.setBlocked(rs.getBoolean("blocked"));
                entity.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                entity.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                return entity;
            }, email);

            return Optional.of(userAccount);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
