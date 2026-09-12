package com.bookstore.backend.mappers;
import com.bookstore.backend.dtos.Book;
import com.bookstore.backend.entities.BookEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring") // Makes it a Spring-managed Bean
public interface BookMapper {
    
    // If field names match exactly, MapStruct maps them automatically
    Book toDto(BookEntity entity);
    
    BookEntity toEntity(Book dto);
}