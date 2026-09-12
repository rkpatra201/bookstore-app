package com.bookstore.backend.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageEntity {
    private Long id;
    private String url;
    private String altText;
    private boolean isPrimary;
    private LocalDateTime createdAt;
}
