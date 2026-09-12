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

    @Test
    void deleteItem_shouldReturnTrueAndRemoveRow_whenItemExistsForUser() {
        // Arrange: Seed an item in the cart for the user
        int itemId = 101;
        CartLineItemEntity lineItem = new CartLineItemEntity();
        lineItem.setUserId(TARGET_USER_ID);
        lineItem.setItemId(itemId);
        lineItem.setQuantity(2);
        cartRepository.saveOrUpdate(lineItem);

        // Act: Delete the item
        boolean deleteResult = cartRepository.deleteItem(TARGET_USER_ID, itemId);

        // Assert: Verify it returns true and the database row is completely gone
        Assertions.assertTrue(deleteResult);

        List<CartLineItemEntity> cartAfterDelete = cartRepository.findByUserId(TARGET_USER_ID);
        Assertions.assertTrue(cartAfterDelete.isEmpty());
    }

    @Test
    void deleteItem_shouldReturnFalse_whenItemDoesNotExist() {
        // Act: Attempt to delete an item that was never added
        boolean deleteResult = cartRepository.deleteItem(TARGET_USER_ID, 999);

        // Assert
        Assertions.assertFalse(deleteResult);
    }

    @Test
    void deleteItem_shouldDeleteRequestedItem_withoutAffectingOtherItemsInUserCart() {
        // Arrange: Seed two different items for the same user
        int itemId = 101;
        CartLineItemEntity itemToDelete = new CartLineItemEntity();
        itemToDelete.setUserId(TARGET_USER_ID);
        itemToDelete.setItemId(itemId); // Item 501
        itemToDelete.setQuantity(2);

        int remainingItemId = 777;
        CartLineItemEntity itemToKeep = new CartLineItemEntity();
        itemToKeep.setUserId(TARGET_USER_ID);
        itemToKeep.setItemId(remainingItemId); // Item 777
        itemToKeep.setQuantity(4);

        cartRepository.saveOrUpdate(itemToDelete);
        cartRepository.saveOrUpdate(itemToKeep);

        // Act: Delete only the first item (501)
        boolean deleteResult = cartRepository.deleteItem(TARGET_USER_ID, itemId);

        // Assert: Verify deletion reported success
        Assertions.assertTrue(deleteResult);

        // Fetch the user's cart to check what remains
        List<CartLineItemEntity> currentCart = cartRepository.findByUserId(TARGET_USER_ID);

        // Ensure exactly one item remains
        Assertions.assertEquals(1, currentCart.size());

        // Verify that the remaining item is indeed the one we wanted to keep (777)
        CartLineItemEntity remainingItem = currentCart.get(0);
        Assertions.assertEquals(TARGET_USER_ID, remainingItem.getUserId());
        Assertions.assertEquals(remainingItemId, remainingItem.getItemId());
        Assertions.assertEquals(4, remainingItem.getQuantity());
    }

    @Test
    void deleteItem_shouldReturnFalseAndNotDelete_whenItemBelongsToDifferentUser() {
        // Arrange: Seed an item for a completely different user
        int itemId1=101;
        CartLineItemEntity otherUserItem = new CartLineItemEntity();
        otherUserItem.setUserId(OTHER_USER_ID);
        otherUserItem.setItemId(itemId1);
        otherUserItem.setQuantity(3);
        cartRepository.saveOrUpdate(otherUserItem);

        // Act: Try to delete that same item ID, but using TARGET_USER_ID's scope
        boolean deleteResult = cartRepository.deleteItem(TARGET_USER_ID, itemId1);

        // Assert: Ensure it returns false because the record doesn't match both conditions
        Assertions.assertFalse(deleteResult);

        // Verify the original user's cart item remains untouched
        List<CartLineItemEntity> otherUserCart = cartRepository.findByUserId(OTHER_USER_ID);
        Assertions.assertEquals(1, otherUserCart.size());
        Assertions.assertEquals(3, otherUserCart.get(0).getQuantity());
    }
}