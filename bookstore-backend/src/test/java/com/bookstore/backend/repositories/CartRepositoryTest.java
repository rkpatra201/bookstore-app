package com.bookstore.backend.repositories;

import com.bookstore.backend.entities.CartLineItemEntity;
import com.bookstore.backend.exceptions.CartException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;


@JdbcTest
@ActiveProfiles("test")
@Import(CartRepository.class)
class CartRepositoryTest {

    private static final String USER_ID = "user-abc-123";
    private static final int TARGET_ITEM_ID = 501;
    private static final int OTHER_ITEM_ID = 777;
    private static final String TARGET_USER_ID = "user-abc-123";
    private static final String OTHER_USER_ID = "user-xyz-789";
    @Autowired
    private CartRepository cartRepository;

    @Test
    void saveOrUpdate_shouldInsertNewItem_whenItemDoesNotExist() {
        // Arrange
        CartLineItemEntity newItem = new CartLineItemEntity();
        newItem.setItemId(101);
        newItem.setQuantity(5);
        newItem.setUserId("user-new-item");

        // Act
        cartRepository.saveOrUpdate(newItem);

        // Assert
        List<CartLineItemEntity> cart = cartRepository.findByUserId("user-new-item");
        Assertions.assertEquals(1, cart.size());
        Assertions.assertEquals(101, cart.get(0).getItemId());
        Assertions.assertEquals(5, cart.get(0).getQuantity());
    }

    @Test
    void saveOrUpdate_shouldIncrementQuantity_whenItemAlreadyExists() {
        // Arrange: First, add an item with quantity 10
        CartLineItemEntity firstAdd = new CartLineItemEntity();
        firstAdd.setItemId(1);
        firstAdd.setQuantity(10);
        firstAdd.setUserId("user-abc");
        cartRepository.saveOrUpdate(firstAdd);

        // Act: Add the same item again with quantity 5
        CartLineItemEntity secondAdd = new CartLineItemEntity();
        secondAdd.setItemId(1);
        secondAdd.setQuantity(5);
        secondAdd.setUserId("user-abc");
        cartRepository.saveOrUpdate(secondAdd);

        // Assert: Quantity should be 10 + 5 = 15
        List<CartLineItemEntity> cart = cartRepository.findByUserId("user-abc");
        Assertions.assertEquals(1, cart.size());
        Assertions.assertEquals(1, cart.get(0).getItemId());
        Assertions.assertEquals(15, cart.get(0).getQuantity());
    }

    @Test
    void saveOrUpdate_shouldHandleMultipleRepeatedAdds_cumulativelyIncreasingQuantity() {
        // Regression test: Simulate a user clicking "Add to Cart" multiple times
        String userId = "user-repeat-clicker";
        int itemId = 42;

        // Arrange & Act: Simulate 5 separate "Add to Cart" clicks, each adding quantity 1
        for (int i = 0; i < 5; i++) {
            CartLineItemEntity addClick = new CartLineItemEntity();
            addClick.setUserId(userId);
            addClick.setItemId(itemId);
            addClick.setQuantity(1);
            cartRepository.saveOrUpdate(addClick);
        }

        // Assert: After 5 clicks of quantity 1, total should be 5
        List<CartLineItemEntity> cart = cartRepository.findByUserId(userId);
        Assertions.assertEquals(1, cart.size());
        Assertions.assertEquals(itemId, cart.get(0).getItemId());
        Assertions.assertEquals(5, cart.get(0).getQuantity());
    }

    @Test
    void saveOrUpdate_shouldMaintainSeparateQuantities_forDifferentItems() {
        // Arrange: User adds two different items, then adds more of the first item
        String userId = "user-multi-item";

        CartLineItemEntity item1FirstAdd = new CartLineItemEntity();
        item1FirstAdd.setUserId(userId);
        item1FirstAdd.setItemId(100);
        item1FirstAdd.setQuantity(3);

        CartLineItemEntity item2FirstAdd = new CartLineItemEntity();
        item2FirstAdd.setUserId(userId);
        item2FirstAdd.setItemId(200);
        item2FirstAdd.setQuantity(2);

        CartLineItemEntity item1SecondAdd = new CartLineItemEntity();
        item1SecondAdd.setUserId(userId);
        item1SecondAdd.setItemId(100);
        item1SecondAdd.setQuantity(4);

        // Act
        cartRepository.saveOrUpdate(item1FirstAdd);
        cartRepository.saveOrUpdate(item2FirstAdd);
        cartRepository.saveOrUpdate(item1SecondAdd);

        // Assert
        List<CartLineItemEntity> cart = cartRepository.findByUserId(userId);
        Assertions.assertEquals(2, cart.size());

        // Verify item 100 has cumulative quantity (3 + 4 = 7)
        CartLineItemEntity actualItem1 = cart.stream()
                .filter(item -> item.getItemId() == 100)
                .findFirst()
                .orElseThrow();
        Assertions.assertEquals(7, actualItem1.getQuantity());

        // Verify item 200 remains unchanged at 2
        CartLineItemEntity actualItem2 = cart.stream()
                .filter(item -> item.getItemId() == 200)
                .findFirst()
                .orElseThrow();
        Assertions.assertEquals(2, actualItem2.getQuantity());
    }

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
        int itemId1 = 101;
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

    @Test
    void reduceItemCount_shouldLowerQuantity_whenRemainingIsGreaterThanZero() {
        // Arrange: Seed a line item with a quantity of 5
        CartLineItemEntity lineItem = new CartLineItemEntity();
        lineItem.setUserId(USER_ID);
        lineItem.setItemId(TARGET_ITEM_ID);
        lineItem.setQuantity(5);
        cartRepository.saveOrUpdate(lineItem);

        // Act: Reduce the item count by 2
        cartRepository.reduceItemCount(USER_ID, TARGET_ITEM_ID, 2);

        // Assert: Verify row remains but quantity drops to 3
        List<CartLineItemEntity> cart = cartRepository.findByUserId(USER_ID);
        Assertions.assertEquals(1, cart.size());
        Assertions.assertEquals(3, cart.get(0).getQuantity());
    }

    @Test
    void reduceItemCount_shouldRemoveRowCompletely_whenQuantityDropsToZeroOrBelow() {
        // Arrange: Seed a line item with a quantity of 3
        CartLineItemEntity lineItem = new CartLineItemEntity();
        lineItem.setUserId(USER_ID);
        lineItem.setItemId(TARGET_ITEM_ID);
        lineItem.setQuantity(3);
        cartRepository.saveOrUpdate(lineItem);

        // Act: Reduce by 3 (exact boundary reduction to zero)
        cartRepository.reduceItemCount(USER_ID, TARGET_ITEM_ID, 3);

        // Assert: The row should be completely purged from the table
        List<CartLineItemEntity> cart = cartRepository.findByUserId(USER_ID);
        Assertions.assertTrue(cart.isEmpty());
    }

    @Test
    void reduceItemCount_shouldOnlyModifyTargetItem_withoutAffectingOtherItemsInUserCart() {
        // Arrange: Seed two different items for the same user
        CartLineItemEntity targetItem = new CartLineItemEntity();
        targetItem.setUserId(USER_ID);
        targetItem.setItemId(TARGET_ITEM_ID); // Item 501
        targetItem.setQuantity(4);

        CartLineItemEntity otherItem = new CartLineItemEntity();
        otherItem.setUserId(USER_ID);
        otherItem.setItemId(OTHER_ITEM_ID); // Item 777
        otherItem.setQuantity(3);

        cartRepository.saveOrUpdate(targetItem);
        cartRepository.saveOrUpdate(otherItem);

        // Act: Reduce the target item by 2
        cartRepository.reduceItemCount(USER_ID, TARGET_ITEM_ID, 2);

        // Assert: Fetch current cart and verify isolation bounds
        List<CartLineItemEntity> currentCart = cartRepository.findByUserId(USER_ID);
        Assertions.assertEquals(2, currentCart.size());

        // Find and check the reduced target item
        CartLineItemEntity actualTarget = currentCart.stream()
                .filter(item -> item.getItemId() == TARGET_ITEM_ID)
                .findFirst()
                .orElseThrow();
        Assertions.assertEquals(2, actualTarget.getQuantity());

        // Find and verify that the other item remains untouched
        CartLineItemEntity actualOther = currentCart.stream()
                .filter(item -> item.getItemId() == OTHER_ITEM_ID)
                .findFirst()
                .orElseThrow();
        Assertions.assertEquals(3, actualOther.getQuantity());
    }

    @Test
    void reduceItemCount_shouldThrowCartException_whenItemDoesNotExist() {
        // Act & Assert: Attempting to reduce an item not present in the cart throws validation error
        CartException exception = Assertions.assertThrows(
                CartException.class,
                () -> cartRepository.reduceItemCount(USER_ID, 999, 1)
        );

        Assertions.assertEquals("Item not found in your cart", exception.getMessage());
    }

    @Test
    void clearCart_shouldWipeAllItemsForTargetUser_withoutAffectingOtherUsers() {
        // Arrange: 1. Seed two items for the target user who is about to check out
        CartLineItemEntity targetItem1 = new CartLineItemEntity();
        targetItem1.setUserId(TARGET_USER_ID);
        targetItem1.setItemId(101);
        targetItem1.setQuantity(2);

        CartLineItemEntity targetItem2 = new CartLineItemEntity();
        targetItem2.setUserId(TARGET_USER_ID);
        targetItem2.setItemId(102);
        targetItem2.setQuantity(1);

        // Arrange: 2. Seed an item for a different user to verify multi-tenant safety bounds
        CartLineItemEntity outsideUserItem = new CartLineItemEntity();
        outsideUserItem.setUserId(OTHER_USER_ID);
        outsideUserItem.setItemId(101);
        outsideUserItem.setQuantity(4);

        // Save records to the in-memory database
        cartRepository.saveOrUpdate(targetItem1);
        cartRepository.saveOrUpdate(targetItem2);
        cartRepository.saveOrUpdate(outsideUserItem);

        // Pre-Verify: Confirm target user has items before clearing
        Assertions.assertEquals(2, cartRepository.findByUserId(TARGET_USER_ID).size());

        // Act: Perform the full cleanup action
        cartRepository.clearCart(TARGET_USER_ID);

        // Assert: 3. Verify target user's cart is now perfectly empty
        List<CartLineItemEntity> clearedCart = cartRepository.findByUserId(TARGET_USER_ID);
        Assertions.assertTrue(clearedCart.isEmpty());

        // Assert: 4. Critical Isolation Check - Verify the other user's items remain untouched
        List<CartLineItemEntity> outsideCart = cartRepository.findByUserId(OTHER_USER_ID);
        Assertions.assertEquals(1, outsideCart.size());
        Assertions.assertEquals(4, outsideCart.get(0).getQuantity());
    }

}