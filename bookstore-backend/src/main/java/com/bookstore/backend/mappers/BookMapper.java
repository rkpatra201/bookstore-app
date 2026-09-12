package com.bookstore.backend.mappers;

import com.bookstore.backend.dtos.Book;
import com.bookstore.backend.entities.BookEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(uses = AuthorMapper.class) // Makes it a Spring-managed Bean
public interface BookMapper {

    BookMapper INSTANCE = Mappers.getMapper(BookMapper.class);

    // If field names match exactly, MapStruct maps them automatically
    Book toDto(BookEntity entity);

    List<Book> toDtoList(List<BookEntity> entity);

    BookEntity toEntity(Book dto);
}