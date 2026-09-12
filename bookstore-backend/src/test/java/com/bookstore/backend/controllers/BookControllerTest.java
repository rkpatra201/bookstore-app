package com.bookstore.backend.controllers;

import com.bookstore.backend.services.BookService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BookControllerTest {

    @Mock
    private BookService bookService;
    @InjectMocks
    private BookController bookController;

    @Test
    void getBooks() {
        Assertions.assertTrue(bookController.getBooks().getStatusCode().is2xxSuccessful());
        Assertions.assertTrue(bookController.getBooks().getBody().getData().isEmpty());
    }
}