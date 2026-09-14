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
public class UserAccountEntity {
    private String userId;
    private String email;
    private String firstName;
    private String lastName;
    private String password;
    private Boolean blocked;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
