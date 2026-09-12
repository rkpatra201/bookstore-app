package com.bookstore.backend.mappers;

import com.bookstore.backend.dtos.LineItemResponse;
import com.bookstore.backend.dtos.OrderDetailsResponse;
import com.bookstore.backend.dtos.OrderLineItemResponse;
import com.bookstore.backend.entities.OrderEntity;
import com.bookstore.backend.entities.OrderLineItemEntity;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface OrderMapper {

    OrderMapper INSTANCE = Mappers.getMapper(OrderMapper.class);

    // MapStruct automatically maps nested lists matching the child mapping rule below
    OrderDetailsResponse toDetailsResponse(OrderEntity entity);

    OrderLineItemResponse toLineItemDto(OrderLineItemEntity entity);

    @Mapping(target = "orderId", source = "orderId")
    @Mapping(target = "id", ignore = true)
        // Database auto-generates this PK row ID
    OrderLineItemEntity toLineItemEntity(LineItemResponse cartItem, Long orderId);

    /**
     * MapStruct automatically generates the collection loop, passing the context value
     * down to the individual row item mapping method defined above!
     */
    List<OrderLineItemEntity> toLineItemEntityList(List<LineItemResponse> cartItems, @Context Long orderId);


}
