package com.bookstore.backend.services;

import com.bookstore.backend.dtos.Cart;
import com.bookstore.backend.dtos.CheckoutRequest;
import com.bookstore.backend.dtos.CustomerAddress;
import com.bookstore.backend.dtos.OrderResponse;
import com.bookstore.backend.entities.OrderEntity;
import com.bookstore.backend.entities.OrderLineItemEntity;
import com.bookstore.backend.repositories.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderService {

    private final CartService cartService;
    private final AddressService addressService;
    private final OrderRepository orderRepository;

    public OrderService(CartService cartService, AddressService addressService, OrderRepository orderRepository) {
        this.cartService = cartService;
        this.addressService = addressService;
        this.orderRepository = orderRepository;
    }

    /**
     * Executes the secure checkout workflow.
     * Transforms active cart details into a finalized order invoice.
     */
    @Transactional // Guarantees the order maps fully and the cart clears out as a single unit
    public OrderResponse checkout(String userId, CheckoutRequest request) {

        // 1. Fetch the authoritative server-side cart snapshot
        Cart cart = cartService.getCart(userId);
        if (cart.getLineItems() == null || cart.getLineItems().isEmpty()) {
            throw new IllegalStateException("Cannot checkout an empty shopping cart");
        }

        // 2. Fetch the target delivery address and verify security ownership bounds
        CustomerAddress address = addressService.getAddressById(request.getAddressId(), userId);

        // 3. Freeze the address into a flat text snapshot format
        String addressSnapshot = formatAddressSnapshot(address);

        // 4. Build and persist the master order record
        OrderEntity masterOrder = OrderEntity.builder()
                .userId(userId)
                .shippingAddressSnapshot(addressSnapshot)
                .totalAmount(cart.getTotalCartPrice())
                .orderStatus("PENDING")
                .build();

        Long orderId = orderRepository.saveMasterOrder(masterOrder);

        // 5. Convert cart line responses into historical snapshot entities and bulk insert
        List<OrderLineItemEntity> lineItemEntities = cart.getLineItems().stream()
                .map(cartItem -> OrderLineItemEntity.builder()
                        .orderId(orderId)
                        .itemId(cartItem.getItemId())
                        .title(cartItem.getTitle())
                        .unitPrice(cartItem.getUnitPrice())
                        .quantity(cartItem.getQuantity())
                        .subTotal(cartItem.getSubTotal())
                        .build())
                .toList();

        orderRepository.saveOrderLineItems(orderId, lineItemEntities);

        // 6. Clear the shopping cart since the transaction is successfully recorded
        cartService.clearCart(userId);

        // 7. Return the summarized response payload
        return new OrderResponse(orderId, "PENDING", cart.getTotalCartPrice());
    }

    /**
     * Helper utility to flatten an address DTO into a standard frozen text snapshot string.
     */
    private String formatAddressSnapshot(CustomerAddress address) {
        StringBuilder sb = new StringBuilder();
        sb.append(address.getRecipientName()).append(", ");
        sb.append(address.getAddressLine1()).append(", ");
        if (address.getAddressLine2() != null && !address.getAddressLine2().isBlank()) {
            sb.append(address.getAddressLine2()).append(", ");
        }
        sb.append(address.getCity()).append(", ");
        sb.append(address.getState()).append(" - ");
        sb.append(address.getPostalCode()).append(", ");
        sb.append(address.getCountry());
        return sb.toString();
    }
}
