package com.bookstore.backend.services;

import com.bookstore.backend.dtos.*;
import com.bookstore.backend.entities.OrderEntity;
import com.bookstore.backend.entities.OrderLineItemEntity;
import com.bookstore.backend.enums.OrderStatus;
import com.bookstore.backend.enums.PaymentMethod;
import com.bookstore.backend.exceptions.ItemNotFoundException;
import com.bookstore.backend.mappers.OrderMapper;
import com.bookstore.backend.repositories.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
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
        log.info("Starting checkout process - User: {}, AddressId: {}, PaymentMethod: {}",
            userId, request.getAddressId(), request.getPaymentMethod());

        try {
            // 1. Fetch the authoritative server-side cart snapshot
            Cart cart = cartService.getCart(userId);
            if (cart.getLineItems() == null || cart.getLineItems().isEmpty()) {
                log.warn("Checkout failed - User: {} - Empty cart", userId);
                throw new IllegalStateException("Cannot checkout an empty shopping cart");
            }

            log.debug("Checkout - User: {} - Cart contains {} items, Total: ${}", userId, cart.getItemCount(), String.format("%.2f", cart.getTotalCartPrice()));

            // 2. Fetch the target delivery address and verify security ownership bounds
            CustomerAddress address = addressService.getAddressById(request.getAddressId(), userId);

            // 3. Validate payment method is provided
            if (request.getPaymentMethod() == null) {
                log.warn("Checkout failed - User: {} - Payment method not provided", userId);
                throw new IllegalArgumentException("Payment method is required for checkout");
            }

            // 4. Freeze the address into a flat text snapshot format
            String addressSnapshot = formatAddressSnapshot(address);

            // 5. Build and persist the master order record
            OrderEntity masterOrder = OrderEntity.builder()
                    .userId(userId)
                    .shippingAddressSnapshot(addressSnapshot)
                    .totalAmount(cart.getTotalCartPrice())
                    .orderStatus(determineInitialOrderStatus(request.getPaymentMethod()))
                    .paymentMethod(request.getPaymentMethod())
                    .build();

            Long orderId = orderRepository.saveMasterOrder(masterOrder);
            log.info("Order created - OrderId: {}, User: {}, PaymentMethod: {}, Status: {}, Total: ${}",
                orderId, userId, request.getPaymentMethod(), masterOrder.getOrderStatus(), String.format("%.2f", cart.getTotalCartPrice()));

            // 5. Mapping of lineItems to lineItemEntities
            List<OrderLineItemEntity> lineItemEntities = OrderMapper.INSTANCE
                    .toLineItemEntityList(cart.getLineItems(), orderId);

            orderRepository.saveOrderLineItems(orderId, lineItemEntities);
            log.debug("Saved {} line items for order: {}", lineItemEntities.size(), orderId);

            // 6. Clear the shopping cart since the transaction is successfully recorded
            cartService.clearCart(userId);

            // 7. Return the summarized response payload
            log.info("Checkout completed successfully - OrderId: {}, User: {}, PaymentMethod: {}, Status: {}, Total: ${}",
                orderId, userId, request.getPaymentMethod(), masterOrder.getOrderStatus(), String.format("%.2f", cart.getTotalCartPrice()));
            return new OrderResponse(orderId, masterOrder.getOrderStatus(), request.getPaymentMethod(), cart.getTotalCartPrice());
        } catch (Exception e) {
            log.error("Checkout failed - User: {}, Error: {}", userId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Determines the initial order status based on payment method.
     * COD orders go to RESERVED (will be paid on delivery).
     * Other payment methods go to AWAITING_PAYMENT (require immediate payment).
     */
    private OrderStatus determineInitialOrderStatus(PaymentMethod paymentMethod) {
        if (paymentMethod == PaymentMethod.COD) {
            return OrderStatus.RESERVED;
        }
        return OrderStatus.AWAITING_PAYMENT;
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

    /**
     * Retrieves an order by its ID ensuring strict ownership context alignment.
     * Maps the database entities into a specialized response DTO hierarchy.
     */
    public OrderDetailsResponse getOrderById(Long orderId, String userId) {
        log.debug("Fetching order details - OrderId: {}, User: {}", orderId, userId);

        // 1. Retrieve the authoritative master-detail model from the repository
        OrderEntity orderEntity = orderRepository.findOrderById(orderId);

        // 2. Security validation: Fail if the order doesn't exist or belongs to someone else
        if (orderEntity == null || !orderEntity.getUserId().equals(userId)) {
            log.warn("Order access denied or not found - OrderId: {}, User: {}", orderId, userId);
            throw new ItemNotFoundException("Order not found or access denied");
        }

        log.info("Order details retrieved - OrderId: {}, User: {}, Status: {}", orderId, userId, orderEntity.getOrderStatus());

        // 3. Use the plain Java MapStruct mapper instance to clean up the builder loops
        return OrderMapper.INSTANCE.toDetailsResponse(orderEntity);
    }

    /**
     * Retrieves the complete summary history of all orders for a specific user ID.
     * Excludes heavy nested child line items to optimize profile listing rendering speed.
     */
    public List<OrderSummaryResponse> getOrderHistory(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }

        log.debug("Fetching order history for user: {}", userId);
        List<OrderEntity> orderEntities = orderRepository.findAllOrdersByUserId(userId);
        log.info("Order history retrieved - User: {}, Total orders: {}", userId, orderEntities.size());

        return OrderMapper.INSTANCE.toSummaryResponseList(orderEntities);
    }


}
