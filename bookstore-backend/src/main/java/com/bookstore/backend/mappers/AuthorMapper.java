package com.bookstore.backend.mappers;
import com.bookstore.backend.dtos.Author;
import com.bookstore.backend.dtos.Book;
import com.bookstore.backend.entities.AuthorEntity;
import com.bookstore.backend.entities.BookEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring") // Makes it a Spring-managed Bean
public interface AuthorMapper {

    // If field names match exactly, MapStruct maps them automatically
    Author toDto(AuthorEntity entity);
    
    AuthorEntity toEntity(Author dto);
}