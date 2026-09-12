package com.bookstore.backend.services;

import com.bookstore.backend.dtos.UserContext;
import org.springframework.stereotype.Service;

@Service
public class UserContextService {

    public UserContext getUserContext() {
        // GET it from thread local, the web-filter must set it to thread local by extracting from headers
        return new UserContext("user123"); //
    }
}
