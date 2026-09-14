package com.bookstore.backend.filters;

import com.bookstore.backend.services.JwtService;
import com.bookstore.backend.services.UserAccountService;
import com.bookstore.backend.services.UserContextService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
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
    private final UserContextService userContextService;

    private static final List<String> PUBLIC_ENDPOINTS = Arrays.asList(
            "/api/user-accounts/registration",
            "/api/user-accounts/login",
            "/api/books",
            "/health"
    );

    public JwtAuthenticationFilter(JwtService jwtService, UserAccountService userAccountService,
                                   UserContextService userContextService) {
        this.jwtService = jwtService;
        this.userAccountService = userAccountService;
        this.userContextService = userContextService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestPath = request.getRequestURI();
        log.debug("Processing request: {} {}", request.getMethod(), requestPath);

        // Check if the endpoint is public
        if (isPublicEndpoint(requestPath) || request.getMethod().equals("OPTIONS")) {
            log.debug("Public endpoint accessed: {}", requestPath);
            filterChain.doFilter(request, response);
            return;
        }

        // Extract token from Authorization header or cookie
        String token = extractTokenFromRequest(request);

        if (token == null) {
            log.warn("Missing or invalid authentication token for request: {}", requestPath);
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Missing or invalid authentication token\"}");
            return;
        }

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
            userContextService.setUserContext(userId);

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.error("Authentication failed: {}", e.getMessage());
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Authentication failed\"}");
        }finally {
          userContextService.clearUserContext();
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

    /**
     * Extracts JWT token from either Authorization header (Bearer token) or x-auth-cookie.
     * Priority: Authorization header takes precedence over cookie.
     *
     * @param request The HTTP request
     * @return The JWT token if found, null otherwise
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        // First, try to extract from Authorization header
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            log.debug("Token found in Authorization header");
            return authHeader.substring(7);
        }

        // If not in header, try to extract from cookie
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("x-auth-cookie".equals(cookie.getName())) {
                    log.debug("Token found in x-auth-cookie");
                    return cookie.getValue();
                }
            }
        }

        log.debug("No token found in Authorization header or x-auth-cookie");
        return null;
    }
}
