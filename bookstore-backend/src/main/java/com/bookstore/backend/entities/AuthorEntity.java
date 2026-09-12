package com.bookstore.backend.entities;

import lombok.Data;

@Data
public class AuthorEntity {
    private int id;
    private String authorName;
    private String authorCode;
}
