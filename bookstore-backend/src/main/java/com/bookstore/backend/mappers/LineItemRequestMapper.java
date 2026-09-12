package com.bookstore.backend.mappers;

import com.bookstore.backend.dtos.Book;
import com.bookstore.backend.dtos.LineItemRequest;
import com.bookstore.backend.entities.BookEntity;
import com.bookstore.backend.entities.CartLineItemEntity;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper // Makes it a Spring-managed Bean
public interface LineItemRequestMapper {

    LineItemRequestMapper INSTANCE = Mappers.getMapper(LineItemRequestMapper.class);
    // If field names match exactly, MapStruct maps them automatically
    LineItemRequest toDto(CartLineItemEntity entity);
    @Mapping(target = "userId", source = "userId")
    CartLineItemEntity toEntity(LineItemRequest request, String userId);
    List<Book> toDtoList(List<BookEntity> entity);

}