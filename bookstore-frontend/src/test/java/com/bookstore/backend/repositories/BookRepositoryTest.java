package com.bookstore.backend.repositories;

import com.bookstore.backend.entities.BookEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@ActiveProfiles("test")
@Import(BookRepository.class)
class BookRepositoryTest {

    @Autowired
    private BookRepository bookRepository;

    @Test
    void shouldFindAllBooksWithTheirAuthors() {
        List<BookEntity> books = bookRepository.findAllBooksWithAuthors();

        // Assert: Asserts against the data you inserted in data.sql
        assertThat(books).isNotEmpty();
        assertThat(books.get(0).getTitle()).isEqualTo("Harry Potter and the Sorcerer's Stone");
        assertThat(books.get(0).getAuthors()).hasSize(1);
    }
}
