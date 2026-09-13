package com.bookstore.backend.mappers;

import com.bookstore.backend.dtos.LineItemRequest;
import com.bookstore.backend.entities.CartLineItemEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper // Makes it a Spring-managed Bean
public interface LineItemRequestMapper {

    LineItemRequestMapper INSTANCE = Mappers.getMapper(LineItemRequestMapper.class);

    // If field names match exactly, MapStruct maps them automatically
    LineItemRequest toDto(CartLineItemEntity entity);

    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "itemId", source = "request.id")
    CartLineItemEntity toEntity(LineItemRequest request, String userId);

}