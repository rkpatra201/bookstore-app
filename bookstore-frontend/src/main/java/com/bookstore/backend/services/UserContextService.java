package com.bookstore.backend.services;

import com.bookstore.backend.dtos.UserContext;
import org.springframework.stereotype.Service;

@Service
public class UserContextService {

    private static final ThreadLocal<String> userContextHolder = new ThreadLocal<>();

    public UserContext getUserContext() {
        String userId = userContextHolder.get();
        if (userId == null || userId.isBlank()) {
            userId = "user123"; // Fallback for development/demo purposes
        }
        return new UserContext(userId);
    }

    public void setUserContext(String userId) {
        userContextHolder.set(userId);
    }

    public void clearUserContext() {
        userContextHolder.remove();
    }
}
