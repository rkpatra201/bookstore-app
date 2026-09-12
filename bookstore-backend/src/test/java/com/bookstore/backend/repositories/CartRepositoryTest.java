package com.bookstore.backend.repositories;

import com.bookstore.backend.entities.CartLineItemEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;


@JdbcTest
@ActiveProfiles("test")
@Import(CartRepository.class)
class CartRepositoryTest {

    @Autowired
    private CartRepository cartRepository;

    @Test
    void saveOrUpdate() {
        CartLineItemEntity cartLineItemEntity = new CartLineItemEntity();
        cartLineItemEntity.setItemId(1);
        cartLineItemEntity.setQuantity(10);
        cartLineItemEntity.setUserId("user-abc");
        cartRepository.saveOrUpdate(cartLineItemEntity);
        cartRepository.saveOrUpdate(cartLineItemEntity);
    }
}