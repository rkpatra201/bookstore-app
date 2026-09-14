package com.bookstore.backend.services;

import com.bookstore.backend.dtos.Book;
import com.bookstore.backend.entities.AuthorEntity;
import com.bookstore.backend.entities.BookEntity;
import com.bookstore.backend.repositories.BookRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookService bookService;

    @Test
    void getEmptyBooks() {
        Mockito.when(bookRepository.findAllBooksWithAuthors()).thenReturn(List.of());
        Assertions.assertTrue(bookService.getBooks().isEmpty());
    }

    @Test
    void getNonEmptyBooks() {
        // --- 1. Create Data for Book 1 (Multiple Authors) ---
        List<AuthorEntity> book1Authors = new ArrayList<>();
        book1Authors.add(new AuthorEntity(1, "JOHN", "AUTH_101"));
        book1Authors.add(new AuthorEntity(2, "ROBERT", "AUTH_102"));

        BookEntity book1 = new BookEntity();
        book1.setId(101);
        book1.setTitle("Spring Framework In Action");
        book1.setAuthors(book1Authors);

        // --- 2. Create Data for Book 2 (Single Author) ---
        List<AuthorEntity> book2Authors = new ArrayList<>();
        book2Authors.add(new AuthorEntity(3, "JANE", "AUTH_201"));

        BookEntity book2 = new BookEntity();
        book2.setId(102);
        book2.setTitle("Clean Code Mastery");
        book2.setAuthors(book2Authors);

        // --- 3. Build the Master Entity List & Mock the Call ---
        List<BookEntity> bookEntities = new ArrayList<>();
        bookEntities.add(book1);
        bookEntities.add(book2);

        Mockito.when(bookRepository.findAllBooksWithAuthors()).thenReturn(bookEntities);

        // --- 4. Execution ---
        List<Book> books = bookService.getBooks();

        // --- 5. DO Assertions ---
        Assertions.assertFalse(books.isEmpty());
        Assertions.assertEquals(2, books.size());

        // Assertions for Book 1
        Book actualBook1 = books.get(0);
        Assertions.assertEquals(101L, actualBook1.getId());
        Assertions.assertEquals("Spring Framework In Action", actualBook1.getTitle());
        Assertions.assertEquals(2, actualBook1.getAuthors().size());
        Assertions.assertEquals("JOHN", actualBook1.getAuthors().get(0).getAuthorName());
        Assertions.assertEquals("ROBERT", actualBook1.getAuthors().get(1).getAuthorName());

        // Assertions for Book 2
        Book actualBook2 = books.get(1);
        Assertions.assertEquals(102L, actualBook2.getId());
        Assertions.assertEquals("Clean Code Mastery", actualBook2.getTitle());
        Assertions.assertEquals(1, actualBook2.getAuthors().size());
        Assertions.assertEquals("JANE", actualBook2.getAuthors().get(0).getAuthorName());
    }
}
