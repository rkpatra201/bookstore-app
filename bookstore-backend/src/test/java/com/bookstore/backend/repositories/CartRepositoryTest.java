package com.bookstore.backend.repositories;

import com.bookstore.backend.entities.CartLineItemEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

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

    private static final String TARGET_USER_ID = "user-abc-123";
    private static final String OTHER_USER_ID = "user-xyz-789";

    @Test
    void findByUserId_shouldReturnEmptyList_whenUserHasNoItems() {
        // Act
        List<CartLineItemEntity> cartItems = cartRepository.findByUserId(TARGET_USER_ID);

        // Assert
        Assertions.assertNotNull(cartItems);
        Assertions.assertTrue(cartItems.isEmpty());
    }

    @Test
    void findByUserId_shouldReturnOnlyItemsBelongingToRequestedUser() {
        // Arrange: Seed items for the targeted user
        CartLineItemEntity targetItem1 = new CartLineItemEntity();
        targetItem1.setUserId(TARGET_USER_ID);
        targetItem1.setItemId(501);
        targetItem1.setQuantity(2);

        CartLineItemEntity targetItem2 = new CartLineItemEntity();
        targetItem2.setUserId(TARGET_USER_ID);
        targetItem2.setItemId(502);
        targetItem2.setQuantity(1);

        // Arrange: Seed an item for a completely different user to verify filtering isolation
        CartLineItemEntity noisyNeighborItem = new CartLineItemEntity();
        noisyNeighborItem.setUserId(OTHER_USER_ID);
        noisyNeighborItem.setItemId(501);
        noisyNeighborItem.setQuantity(5);

        // Persist all records
        cartRepository.saveOrUpdate(targetItem1);
        cartRepository.saveOrUpdate(targetItem2);
        cartRepository.saveOrUpdate(noisyNeighborItem);

        // Act
        List<CartLineItemEntity> activeCart = cartRepository.findByUserId(TARGET_USER_ID);

        // Assert: Verify size and that noise data from the other user was ignored
        Assertions.assertNotNull(activeCart);
        Assertions.assertEquals(2, activeCart.size());

        // Validate first matched entity contents
        CartLineItemEntity actualItem1 = activeCart.get(0);
        Assertions.assertEquals(TARGET_USER_ID, actualItem1.getUserId());
        Assertions.assertEquals(501, actualItem1.getItemId());
        Assertions.assertEquals(2, actualItem1.getQuantity());

        // Validate second matched entity contents
        CartLineItemEntity actualItem2 = activeCart.get(1);
        Assertions.assertEquals(TARGET_USER_ID, actualItem2.getUserId());
        Assertions.assertEquals(502, actualItem2.getItemId());
        Assertions.assertEquals(1, actualItem2.getQuantity());
    }
}