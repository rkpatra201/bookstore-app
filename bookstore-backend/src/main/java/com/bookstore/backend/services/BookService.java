package com.bookstore.backend.services;

import com.bookstore.backend.dtos.Book;
import com.bookstore.backend.mappers.BookMapper;
import com.bookstore.backend.repositories.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BookService {

    @Autowired
    private BookRepository bookRepository;

    private BookMapper bookMapper = BookMapper.INSTANCE;

    public List<Book> getBooks(){
      return bookMapper.toDtoList(bookRepository.findAllBooksWithAuthors());
    }
}
