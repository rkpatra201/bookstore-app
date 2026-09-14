package com.bookstore.backend.mappers;

import com.bookstore.backend.dtos.Author;
import com.bookstore.backend.entities.AuthorEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper // Makes it a Spring-managed Bean
public interface AuthorMapper {

    AuthorMapper INSTANCE = Mappers.getMapper(AuthorMapper.class);

    // If field names match exactly, MapStruct maps them automatically
    Author toDto(AuthorEntity entity);

    AuthorEntity toEntity(Author dto);
}