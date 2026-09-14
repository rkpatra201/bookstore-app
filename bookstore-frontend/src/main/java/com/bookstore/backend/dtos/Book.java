package com.bookstore.backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class Book {
    private int id;
    private String title;
    private List<Author> authors;
    private List<Image> images;
    private float price;
    private int stockQty;
}
