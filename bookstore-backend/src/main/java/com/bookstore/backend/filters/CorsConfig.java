package com.bookstore.backend.filters;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // 1. Allow credentials (cookies, authorization headers)
        config.setAllowCredentials(true);

        // 2. Specify allowed frontend source domains explicitly (Production requirement)
        config.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:5173")); 
        // Note: Use config.addAllowedOriginPattern("*") ONLY for internal open dev spaces

        // 3. Define allowed HTTP action methods
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // 4. Define allowed request request header properties
        config.setAllowedHeaders(List.of("Authorization", "Cache-Control", "Content-Type"));

        // 5. Register configuration rules globally across all api route match filters
        source.registerCorsConfiguration("/**", config);
        
        return new CorsFilter(source);
    }
}
