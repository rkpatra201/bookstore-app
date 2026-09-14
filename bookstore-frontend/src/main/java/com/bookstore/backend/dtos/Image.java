package com.bookstore.backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Image {
    private Long id;
    private String url;
    private boolean isPrimary;
    private String altText;
}
