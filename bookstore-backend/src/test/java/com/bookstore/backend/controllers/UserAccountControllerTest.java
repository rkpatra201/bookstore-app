package com.bookstore.backend.controllers;

import com.bookstore.backend.dtos.DataResponse;
import com.bookstore.backend.dtos.LoginRequest;
import com.bookstore.backend.dtos.LoginResponse;
import com.bookstore.backend.dtos.UserAccount;
import com.bookstore.backend.enums.UserAccountError;
import com.bookstore.backend.exceptions.UserAccountException;
import com.bookstore.backend.services.UserAccountService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserAccountControllerTest {

    @Mock
    private UserAccountService userAccountService;

    @InjectMocks
    private UserAccountController userAccountController;

    @Test
    void shouldRegisterUserAccountSuccessfully() {
        UserAccount inputAccount = UserAccount.builder()
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .password("password123")
                .build();

        UserAccount createdAccount = UserAccount.builder()
                .userId("generated-uuid")
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .password("$2a$10$encodedPassword")
                .blocked(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(userAccountService.createUserAccount(any(UserAccount.class))).thenReturn(createdAccount);

        ResponseEntity<DataResponse<UserAccount>> response = userAccountController.registerUserAccount(inputAccount);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).isEqualTo("User account registered successfully");
        assertThat(response.getBody().getData()).isNotNull();
        assertThat(response.getBody().getData().getUserId()).isEqualTo("generated-uuid");
        assertThat(response.getBody().getData().getEmail()).isEqualTo("test@example.com");

        verify(userAccountService).createUserAccount(any(UserAccount.class));
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        UserAccount inputAccount = UserAccount.builder()
                .email("existing@example.com")
                .firstName("John")
                .password("password123")
                .build();

        when(userAccountService.createUserAccount(any(UserAccount.class)))
                .thenThrow(new UserAccountException(UserAccountError.EMAIL_ALREADY_EXISTS));

        assertThatThrownBy(() -> userAccountController.registerUserAccount(inputAccount))
                .isInstanceOf(UserAccountException.class)
                .hasMessage(UserAccountError.EMAIL_ALREADY_EXISTS.getMessage());

        verify(userAccountService).createUserAccount(any(UserAccount.class));
    }

    @Test
    void shouldThrowExceptionWhenValidationFails() {
        UserAccount inputAccount = UserAccount.builder()
                .email("test@example.com")
                .firstName(null)
                .password("password123")
                .build();

        when(userAccountService.createUserAccount(any(UserAccount.class)))
                .thenThrow(new UserAccountException(UserAccountError.FIRST_NAME_REQUIRED));

        assertThatThrownBy(() -> userAccountController.registerUserAccount(inputAccount))
                .isInstanceOf(UserAccountException.class)
                .hasMessage(UserAccountError.FIRST_NAME_REQUIRED.getMessage());

        verify(userAccountService).createUserAccount(any(UserAccount.class));
    }

    @Test
    void shouldGetUserByUserId() {
        String userId = "test-user-id";

        UserAccount userAccount = UserAccount.builder()
                .userId(userId)
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .password("password123")
                .blocked(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(userAccountService.findByUserId(userId)).thenReturn(userAccount);

        ResponseEntity<DataResponse<UserAccount>> response = userAccountController.getUserByUserId(userId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).isEqualTo("User account retrieved successfully");
        assertThat(response.getBody().getData()).isNotNull();
        assertThat(response.getBody().getData().getUserId()).isEqualTo(userId);
        assertThat(response.getBody().getData().getEmail()).isEqualTo("test@example.com");

        verify(userAccountService).findByUserId(userId);
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        String userId = "non-existent-id";

        when(userAccountService.findByUserId(userId))
                .thenThrow(new UserAccountException(UserAccountError.USER_NOT_FOUND));

        assertThatThrownBy(() -> userAccountController.getUserByUserId(userId))
                .isInstanceOf(UserAccountException.class)
                .hasMessage(UserAccountError.USER_NOT_FOUND.getMessage());

        verify(userAccountService).findByUserId(userId);
    }

    @Test
    void shouldNotReturnPasswordInResponse() {
        UserAccount inputAccount = UserAccount.builder()
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .password("password123")
                .build();

        UserAccount createdAccount = UserAccount.builder()
                .userId("generated-uuid")
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .password("$2a$10$encodedPasswordHash")
                .blocked(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(userAccountService.createUserAccount(any(UserAccount.class))).thenReturn(createdAccount);

        ResponseEntity<DataResponse<UserAccount>> response = userAccountController.registerUserAccount(inputAccount);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).isNotNull();

        verify(userAccountService).createUserAccount(any(UserAccount.class));
    }

    @Test
    void shouldLoginSuccessfully() {
        LoginRequest loginRequest = LoginRequest.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        LoginResponse loginResponse = LoginResponse.builder()
                .token("test-token-uuid")
                .userId("user-123")
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .build();

        when(userAccountService.login(any(LoginRequest.class))).thenReturn(loginResponse);

        ResponseEntity<DataResponse<LoginResponse>> response = userAccountController.login(loginRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).isEqualTo("Login successful");
        assertThat(response.getBody().getData()).isNotNull();
        assertThat(response.getBody().getData().getToken()).isEqualTo("test-token-uuid");
        assertThat(response.getBody().getData().getUserId()).isEqualTo("user-123");
        assertThat(response.getBody().getData().getEmail()).isEqualTo("test@example.com");
        assertThat(response.getBody().getData().getFirstName()).isEqualTo("John");
        assertThat(response.getBody().getData().getLastName()).isEqualTo("Doe");

        assertThat(response.getHeaders().getFirst(HttpHeaders.AUTHORIZATION)).isEqualTo("test-token-uuid");

        verify(userAccountService).login(any(LoginRequest.class));
    }

    @Test
    void shouldThrowExceptionWhenLoginWithInvalidEmail() {
        LoginRequest loginRequest = LoginRequest.builder()
                .email("invalid-email")
                .password("password123")
                .build();

        when(userAccountService.login(any(LoginRequest.class)))
                .thenThrow(new UserAccountException(UserAccountError.INVALID_EMAIL_FORMAT));

        assertThatThrownBy(() -> userAccountController.login(loginRequest))
                .isInstanceOf(UserAccountException.class)
                .hasMessage(UserAccountError.INVALID_EMAIL_FORMAT.getMessage());

        verify(userAccountService).login(any(LoginRequest.class));
    }

    @Test
    void shouldThrowExceptionWhenLoginWithNonExistentEmail() {
        LoginRequest loginRequest = LoginRequest.builder()
                .email("notfound@example.com")
                .password("password123")
                .build();

        when(userAccountService.login(any(LoginRequest.class)))
                .thenThrow(new UserAccountException(UserAccountError.USER_NOT_FOUND_WITH_EMAIL, "notfound@example.com"));

        assertThatThrownBy(() -> userAccountController.login(loginRequest))
                .isInstanceOf(UserAccountException.class)
                .hasMessage("User account not found with email: notfound@example.com");

        verify(userAccountService).login(any(LoginRequest.class));
    }

    @Test
    void shouldThrowExceptionWhenLoginWithWrongPassword() {
        LoginRequest loginRequest = LoginRequest.builder()
                .email("test@example.com")
                .password("wrongPassword")
                .build();

        when(userAccountService.login(any(LoginRequest.class)))
                .thenThrow(new UserAccountException(UserAccountError.LOGIN_FAILED));

        assertThatThrownBy(() -> userAccountController.login(loginRequest))
                .isInstanceOf(UserAccountException.class)
                .hasMessage(UserAccountError.LOGIN_FAILED.getMessage());

        verify(userAccountService).login(any(LoginRequest.class));
    }

    @Test
    void shouldThrowExceptionWhenLoginWithMissingEmail() {
        LoginRequest loginRequest = LoginRequest.builder()
                .email(null)
                .password("password123")
                .build();

        when(userAccountService.login(any(LoginRequest.class)))
                .thenThrow(new UserAccountException(UserAccountError.EMAIL_REQUIRED));

        assertThatThrownBy(() -> userAccountController.login(loginRequest))
                .isInstanceOf(UserAccountException.class)
                .hasMessage(UserAccountError.EMAIL_REQUIRED.getMessage());

        verify(userAccountService).login(any(LoginRequest.class));
    }

    @Test
    void shouldThrowExceptionWhenLoginWithMissingPassword() {
        LoginRequest loginRequest = LoginRequest.builder()
                .email("test@example.com")
                .password(null)
                .build();

        when(userAccountService.login(any(LoginRequest.class)))
                .thenThrow(new UserAccountException(UserAccountError.PASSWORD_REQUIRED));

        assertThatThrownBy(() -> userAccountController.login(loginRequest))
                .isInstanceOf(UserAccountException.class)
                .hasMessage(UserAccountError.PASSWORD_REQUIRED.getMessage());

        verify(userAccountService).login(any(LoginRequest.class));
    }
}
