package com.bookstore.backend.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class AuthorEntity {
    private int id;
    private String authorName;
    private String authorCode;
}
