package com.bookstore.backend.dtos;

import lombok.Data;

import java.util.List;

@Data
public class Book {
    private int id;
    private String title;
    private List<Author> authors;
    private float price;
    private int stockQty;
}
