package com.bookstore.backend.filters;

import com.bookstore.backend.services.JwtService;
import com.bookstore.backend.services.UserAccountService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserAccountService userAccountService;

    private static final List<String> PUBLIC_ENDPOINTS = Arrays.asList(
            "/api/user-accounts/registration",
            "/api/user-accounts/login",
            "/api/books",
            "/health"
    );

    public JwtAuthenticationFilter(JwtService jwtService, UserAccountService userAccountService) {
        this.jwtService = jwtService;
        this.userAccountService = userAccountService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestPath = request.getRequestURI();
        log.debug("Processing request: {} {}", request.getMethod(), requestPath);

        // Check if the endpoint is public
        if (isPublicEndpoint(requestPath)) {
            log.debug("Public endpoint accessed: {}", requestPath);
            filterChain.doFilter(request, response);
            return;
        }

        // Extract token from Authorization header
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing or invalid Authorization header for request: {}", requestPath);
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Missing or invalid Authorization header\"}");
            return;
        }

        String token = authHeader.substring(7);

        // Validate token
        if (!jwtService.validateToken(token)) {
            log.warn("Invalid or expired JWT token for request: {}", requestPath);
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Invalid or expired token\"}");
            return;
        }

        // Extract userId and verify user exists
        try {
            String userId = jwtService.extractUserId(token);
            log.debug("Extracted userId from token: {}", userId);

            // Verify user exists in the system
            userAccountService.findByUserId(userId);
            log.debug("User authenticated successfully: {}", userId);

            // Store userId in request attribute for downstream use
            request.setAttribute("userId", userId);

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.error("Authentication failed: {}", e.getMessage());
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Authentication failed\"}");
        }
    }

    private boolean isPublicEndpoint(String requestPath) {
        return PUBLIC_ENDPOINTS.stream()
                .anyMatch(publicPath -> {
                    if (publicPath.equals("/api/books")) {
                        // Allow all /api/books endpoints (GET, POST, etc.)
                        return requestPath.startsWith(publicPath);
                    }
                    return requestPath.equals(publicPath);
                });
    }
}
