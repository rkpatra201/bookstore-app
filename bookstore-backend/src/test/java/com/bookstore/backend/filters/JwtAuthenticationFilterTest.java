package com.bookstore.backend.filters;

import com.bookstore.backend.dtos.UserAccount;
import com.bookstore.backend.services.JwtService;
import com.bookstore.backend.services.UserAccountService;
import com.bookstore.backend.services.UserContextService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserAccountService userAccountService;

    @Mock
    private UserContextService userContextService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(jwtService, userAccountService, userContextService);
    }

    @Test
    void shouldAllowAccessToPublicEndpoints() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/user-accounts/registration");
        when(request.getMethod()).thenReturn("POST");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    void shouldAllowAccessToLoginEndpoint() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/user-accounts/login");
        when(request.getMethod()).thenReturn("POST");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    void shouldAllowAccessToBookEndpoints() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/books");
        when(request.getMethod()).thenReturn("GET");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    void shouldAllowAccessToHealthEndpoint() throws Exception {
        when(request.getRequestURI()).thenReturn("/health");
        when(request.getMethod()).thenReturn("GET");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    void shouldReturn401WhenAuthorizationHeaderIsMissing() throws Exception {
        StringWriter responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
        when(request.getRequestURI()).thenReturn("/api/orders");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getCookies()).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(401);
        verify(filterChain, never()).doFilter(request, response);
        assertThat(responseWriter.toString()).contains("Missing or invalid authentication token");
    }

    @Test
    void shouldReturn401WhenAuthorizationHeaderIsInvalid() throws Exception {
        StringWriter responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
        when(request.getRequestURI()).thenReturn("/api/orders");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authorization")).thenReturn("InvalidHeader");
        when(request.getCookies()).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(401);
        verify(filterChain, never()).doFilter(request, response);
        assertThat(responseWriter.toString()).contains("Missing or invalid authentication token");
    }

    @Test
    void shouldReturn401WhenTokenIsInvalid() throws Exception {
        StringWriter responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
        when(request.getRequestURI()).thenReturn("/api/orders");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid-token");
        when(jwtService.validateToken("invalid-token")).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(401);
        verify(filterChain, never()).doFilter(request, response);
        assertThat(responseWriter.toString()).contains("Invalid or expired token");
    }

    @Test
    void shouldAuthenticateSuccessfullyWithValidToken() throws Exception {
        String token = "valid-token";
        String userId = "user-123";

        when(request.getRequestURI()).thenReturn("/api/orders");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.validateToken(token)).thenReturn(true);
        when(jwtService.extractUserId(token)).thenReturn(userId);
        when(userAccountService.findByUserId(userId)).thenReturn(UserAccount.builder()
                .userId(userId)
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .build());

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(request).setAttribute("userId", userId);
        verify(response, never()).setStatus(401);
    }

    @Test
    void shouldReturn401WhenUserNotFound() throws Exception {
        StringWriter responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
        String token = "valid-token";
        String userId = "non-existent-user";

        when(request.getRequestURI()).thenReturn("/api/orders");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.validateToken(token)).thenReturn(true);
        when(jwtService.extractUserId(token)).thenReturn(userId);
        when(userAccountService.findByUserId(userId)).thenThrow(new RuntimeException("User not found"));

        filter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(401);
        verify(filterChain, never()).doFilter(request, response);
        assertThat(responseWriter.toString()).contains("Authentication failed");
    }

    @Test
    void shouldAuthenticateSuccessfullyWithCookie() throws Exception {
        // Arrange
        String token = "cookie-token-123";
        String userId = "user-456";

        Cookie authCookie = new Cookie("x-auth-cookie", token);
        when(request.getRequestURI()).thenReturn("/api/cart");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authorization")).thenReturn(null); // No Authorization header
        when(request.getCookies()).thenReturn(new Cookie[]{authCookie});
        when(jwtService.validateToken(token)).thenReturn(true);
        when(jwtService.extractUserId(token)).thenReturn(userId);
        when(userAccountService.findByUserId(userId)).thenReturn(UserAccount.builder()
                .userId(userId)
                .email("test@example.com")
                .firstName("Jane")
                .lastName("Smith")
                .build());

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        verify(request).setAttribute("userId", userId);
        verify(response, never()).setStatus(401);
        verify(userContextService).setUserContext(userId);
    }

    @Test
    void shouldPreferAuthorizationHeaderOverCookie() throws Exception {
        // Arrange
        String headerToken = "header-token";
        String cookieToken = "cookie-token";
        String userId = "user-789";

        Cookie authCookie = new Cookie("x-auth-cookie", cookieToken);
        when(request.getRequestURI()).thenReturn("/api/orders");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + headerToken);
        when(jwtService.validateToken(headerToken)).thenReturn(true);
        when(jwtService.extractUserId(headerToken)).thenReturn(userId);
        when(userAccountService.findByUserId(userId)).thenReturn(UserAccount.builder()
                .userId(userId)
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .build());

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(jwtService).validateToken(headerToken);
        verify(jwtService, never()).validateToken(cookieToken);
        verify(filterChain).doFilter(request, response);
        // Verify cookies were never checked because header took precedence
        verify(request, never()).getCookies();
    }

    @Test
    void shouldReturn401WhenCookieTokenIsInvalid() throws Exception {
        // Arrange
        StringWriter responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
        String token = "invalid-cookie-token";

        Cookie authCookie = new Cookie("x-auth-cookie", token);
        when(request.getRequestURI()).thenReturn("/api/orders");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getCookies()).thenReturn(new Cookie[]{authCookie});
        when(jwtService.validateToken(token)).thenReturn(false);

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(response).setStatus(401);
        verify(filterChain, never()).doFilter(request, response);
        assertThat(responseWriter.toString()).contains("Invalid or expired token");
    }

    @Test
    void shouldReturn401WhenNoCookiesPresent() throws Exception {
        // Arrange
        StringWriter responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
        when(request.getRequestURI()).thenReturn("/api/orders");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getCookies()).thenReturn(null);

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(response).setStatus(401);
        verify(filterChain, never()).doFilter(request, response);
        assertThat(responseWriter.toString()).contains("Missing or invalid authentication token");
    }

    @Test
    void shouldReturn401WhenAuthCookieNotPresent() throws Exception {
        // Arrange
        StringWriter responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));

        Cookie otherCookie = new Cookie("other-cookie", "some-value");
        when(request.getRequestURI()).thenReturn("/api/orders");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getCookies()).thenReturn(new Cookie[]{otherCookie});

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(response).setStatus(401);
        verify(filterChain, never()).doFilter(request, response);
        assertThat(responseWriter.toString()).contains("Missing or invalid authentication token");
    }

    @Test
    void shouldHandleMultipleCookiesAndFindAuthCookie() throws Exception {
        // Arrange
        String token = "valid-cookie-token";
        String userId = "user-999";

        Cookie sessionCookie = new Cookie("JSESSIONID", "session-value");
        Cookie authCookie = new Cookie("x-auth-cookie", token);
        Cookie otherCookie = new Cookie("preferences", "theme=dark");

        when(request.getRequestURI()).thenReturn("/api/cart");
        when(request.getMethod()).thenReturn("POST");
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getCookies()).thenReturn(new Cookie[]{sessionCookie, authCookie, otherCookie});
        when(jwtService.validateToken(token)).thenReturn(true);
        when(jwtService.extractUserId(token)).thenReturn(userId);
        when(userAccountService.findByUserId(userId)).thenReturn(UserAccount.builder()
                .userId(userId)
                .email("user@example.com")
                .firstName("Test")
                .lastName("User")
                .build());

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        verify(request).setAttribute("userId", userId);
        verify(response, never()).setStatus(401);
    }
}
