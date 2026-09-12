package com.bookstore.backend.services;

import com.bookstore.backend.dtos.Book;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BookService {

    public List<Book> getBooks(){
      return new ArrayList<>();
    }
}
