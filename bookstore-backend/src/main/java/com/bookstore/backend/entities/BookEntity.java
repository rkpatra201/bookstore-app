package com.bookstore.backend.entities;

import lombok.Data;

import java.util.List;

@Data
public class BookEntity {
    private int id;
    private String title;
    private List<AuthorEntity> authors;
    private List<ImageEntity> images;
    private float price;
    private int stockQty;
}
