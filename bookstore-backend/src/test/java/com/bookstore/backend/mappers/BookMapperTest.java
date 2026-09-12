package com.bookstore.backend.mappers;

import com.bookstore.backend.dtos.Book;
import com.bookstore.backend.entities.AuthorEntity;
import com.bookstore.backend.entities.BookEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mapstruct.factory.Mappers;

import java.util.List;

@ExtendWith(MockitoExtension.class) // Enables Mockito annotations
class BookMapperTest {

    @Spy // Instantiates the real implementation of AuthorMapper
    private AuthorMapper authorMapper = Mappers.getMapper(AuthorMapper.class);

    @InjectMocks // Instantiates BookMapper and injects the spied authorMapper into it
    private BookMapper bookMapper = Mappers.getMapper(BookMapper.class);

    @Test
    void shouldMapBookWithAuthors() {
        // Arrange
        BookEntity entity = new BookEntity();
        entity.setId(1);
        entity.setTitle("Linear Algebra");
        entity.setPrice(100.43f);
        entity.setStockQty(100);
        entity.setAuthors(List.of(new AuthorEntity(1, "John", "AUTH_101")));

        // Act
        Book dto = bookMapper.toDto(entity);

        // Assert
        Assertions.assertSame(dto.getId(), entity.getId());
        Assertions.assertSame(dto.getTitle(), entity.getTitle());
        Assertions.assertEquals(dto.getPrice(), entity.getPrice());
        Assertions.assertSame(dto.getStockQty(), entity.getStockQty());

        Assertions.assertFalse(dto.getAuthors().isEmpty());
        Assertions.assertEquals("John", dto.getAuthors().get(0).getAuthorName());
        Assertions.assertEquals("AUTH_101", dto.getAuthors().get(0).getAuthorCode());

    }
}
