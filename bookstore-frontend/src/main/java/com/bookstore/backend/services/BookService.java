package com.bookstore.backend.services;

import com.bookstore.backend.dtos.Book;
import com.bookstore.backend.entities.BookEntity;
import com.bookstore.backend.exceptions.ItemNotFoundException;
import com.bookstore.backend.mappers.BookMapper;
import com.bookstore.backend.repositories.BookRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final BookMapper bookMapper = BookMapper.INSTANCE;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public List<Book> getBooks() {
        return bookMapper.toDtoList(bookRepository.findAllBooksWithAuthors());
    }

    public Book getBookById(int id) {
        BookEntity bookEntity = bookRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("Book not found with ID: " + id));
        return bookMapper.toDto(bookEntity);
    }
}
