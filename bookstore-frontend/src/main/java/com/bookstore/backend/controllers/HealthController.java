package com.bookstore.backend.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
@RequestMapping
public class HealthController {

    @GetMapping("/health")
    public String getHealth() {
        return new Date() + ": OK";
    }
}
