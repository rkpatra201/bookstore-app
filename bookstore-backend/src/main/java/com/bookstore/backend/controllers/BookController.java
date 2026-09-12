package com.bookstore.backend.controllers;

import com.bookstore.backend.dtos.Book;
import com.bookstore.backend.services.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/books")
public class BookController {

    @Autowired
    private BookService bookService;

    @GetMapping
    public ResponseEntity<List<Book>> getBooks() {
        return ResponseEntity.status(HttpStatus.OK)
                .body(bookService.getBooks());
    }

}
