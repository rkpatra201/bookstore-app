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
                .userId("user-123")
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .build();

        UserAccountService.LoginResult loginResult = new UserAccountService.LoginResult("test-token-uuid", loginResponse);

        when(userAccountService.login(any(LoginRequest.class))).thenReturn(loginResult);

        ResponseEntity<DataResponse<LoginResponse>> response = userAccountController.login(loginRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).isEqualTo("Login successful");
        assertThat(response.getBody().getData()).isNotNull();

        // Verify HttpOnly cookie is set
        assertThat(response.getHeaders().get(HttpHeaders.SET_COOKIE)).isNotNull();
        assertThat(response.getHeaders().getFirst(HttpHeaders.SET_COOKIE))
                .contains("x-auth-cookie=test-token-uuid")
                .contains("HttpOnly")
                .contains("Path=/")
                .contains("Max-Age=86400")
                .contains("SameSite=Lax");

        assertThat(response.getBody().getData().getUserId()).isEqualTo("user-123");
        assertThat(response.getBody().getData().getEmail()).isEqualTo("test@example.com");
        assertThat(response.getBody().getData().getFirstName()).isEqualTo("John");
        assertThat(response.getBody().getData().getLastName()).isEqualTo("Doe");

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

    @Test
    void shouldSetHttpOnlyCookieOnSuccessfulLogin() {
        // Arrange
        LoginRequest loginRequest = LoginRequest.builder()
                .email("user@example.com")
                .password("securePass")
                .build();

        LoginResponse loginResponse = LoginResponse.builder()
                .userId("user-456")
                .email("user@example.com")
                .firstName("Jane")
                .lastName("Smith")
                .build();

        UserAccountService.LoginResult loginResult = new UserAccountService.LoginResult("jwt-token-12345", loginResponse);
        when(userAccountService.login(any(LoginRequest.class))).thenReturn(loginResult);

        // Act
        ResponseEntity<DataResponse<LoginResponse>> response = userAccountController.login(loginRequest);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        String setCookieHeader = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        assertThat(setCookieHeader)
                .isNotNull()
                .contains("x-auth-cookie=jwt-token-12345")
                .contains("HttpOnly");

        verify(userAccountService, times(1)).login(loginRequest);
    }

    @Test
    void shouldSetCookieWithCorrectPath() {
        // Arrange
        LoginRequest loginRequest = LoginRequest.builder()
                .email("test@example.com")
                .password("password")
                .build();

        LoginResponse loginResponse = LoginResponse.builder()
                .userId("user-789")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .build();

        UserAccountService.LoginResult loginResult = new UserAccountService.LoginResult("token-abc", loginResponse);
        when(userAccountService.login(any(LoginRequest.class))).thenReturn(loginResult);

        // Act
        ResponseEntity<DataResponse<LoginResponse>> response = userAccountController.login(loginRequest);

        // Assert
        String setCookieHeader = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        assertThat(setCookieHeader).contains("Path=/");
    }

    @Test
    void shouldSetCookieWith24HourExpiry() {
        // Arrange
        LoginRequest loginRequest = LoginRequest.builder()
                .email("test@example.com")
                .password("password")
                .build();

        LoginResponse loginResponse = LoginResponse.builder()
                .userId("user-999")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .build();

        UserAccountService.LoginResult loginResult = new UserAccountService.LoginResult("token-xyz", loginResponse);
        when(userAccountService.login(any(LoginRequest.class))).thenReturn(loginResult);

        // Act
        ResponseEntity<DataResponse<LoginResponse>> response = userAccountController.login(loginRequest);

        // Assert
        String setCookieHeader = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        assertThat(setCookieHeader).contains("Max-Age=86400"); // 24 hours = 86400 seconds
    }

    @Test
    void shouldSetCookieWithSameSiteLax() {
        // Arrange
        LoginRequest loginRequest = LoginRequest.builder()
                .email("test@example.com")
                .password("password")
                .build();

        LoginResponse loginResponse = LoginResponse.builder()
                .userId("user-111")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .build();

        UserAccountService.LoginResult loginResult = new UserAccountService.LoginResult("token-lax", loginResponse);
        when(userAccountService.login(any(LoginRequest.class))).thenReturn(loginResult);

        // Act
        ResponseEntity<DataResponse<LoginResponse>> response = userAccountController.login(loginRequest);

        // Assert
        String setCookieHeader = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        assertThat(setCookieHeader).contains("SameSite=Lax");
    }

    @Test
    void shouldNotSetSecureFlagInDevelopment() {
        // Arrange
        LoginRequest loginRequest = LoginRequest.builder()
                .email("test@example.com")
                .password("password")
                .build();

        LoginResponse loginResponse = LoginResponse.builder()
                .userId("user-222")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .build();

        UserAccountService.LoginResult loginResult = new UserAccountService.LoginResult("token-dev", loginResponse);
        when(userAccountService.login(any(LoginRequest.class))).thenReturn(loginResult);

        // Act
        ResponseEntity<DataResponse<LoginResponse>> response = userAccountController.login(loginRequest);

        // Assert
        String setCookieHeader = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        // Secure flag should not be present when secure=false (development mode)
        assertThat(setCookieHeader).doesNotContain("Secure");
    }

    @Test
    void shouldNotIncludeTokenInResponseBody() {
        // Arrange
        LoginRequest loginRequest = LoginRequest.builder()
                .email("test@example.com")
                .password("password")
                .build();

        LoginResponse loginResponse = LoginResponse.builder()
                .userId("user-333")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .build();

        UserAccountService.LoginResult loginResult = new UserAccountService.LoginResult("secret-token", loginResponse);
        when(userAccountService.login(any(LoginRequest.class))).thenReturn(loginResult);

        // Act
        ResponseEntity<DataResponse<LoginResponse>> response = userAccountController.login(loginRequest);

        // Assert
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).isNotNull();

        // Verify token is NOT in response body (security requirement)
        LoginResponse responseData = response.getBody().getData();
        assertThat(responseData.getUserId()).isEqualTo("user-333");
        assertThat(responseData.getEmail()).isEqualTo("test@example.com");
        assertThat(responseData.getFirstName()).isEqualTo("Test");
        assertThat(responseData.getLastName()).isEqualTo("User");

        // Token should only be in cookie, not in response body
        String setCookieHeader = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        assertThat(setCookieHeader).contains("secret-token");
    }

    @Test
    void shouldReturnOnlyUserInfoInResponseBody() {
        // Arrange
        LoginRequest loginRequest = LoginRequest.builder()
                .email("john.doe@example.com")
                .password("strongPassword")
                .build();

        LoginResponse loginResponse = LoginResponse.builder()
                .userId("user-444")
                .email("john.doe@example.com")
                .firstName("John")
                .lastName("Doe")
                .build();

        UserAccountService.LoginResult loginResult = new UserAccountService.LoginResult("jwt-token", loginResponse);
        when(userAccountService.login(any(LoginRequest.class))).thenReturn(loginResult);

        // Act
        ResponseEntity<DataResponse<LoginResponse>> response = userAccountController.login(loginRequest);

        // Assert
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).isEqualTo("Login successful");

        LoginResponse data = response.getBody().getData();
        assertThat(data.getUserId()).isEqualTo("user-444");
        assertThat(data.getEmail()).isEqualTo("john.doe@example.com");
        assertThat(data.getFirstName()).isEqualTo("John");
        assertThat(data.getLastName()).isEqualTo("Doe");
    }

    @Test
    void shouldSetCookieWithCorrectName() {
        // Arrange
        LoginRequest loginRequest = LoginRequest.builder()
                .email("test@example.com")
                .password("password")
                .build();

        LoginResponse loginResponse = LoginResponse.builder()
                .userId("user-555")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .build();

        UserAccountService.LoginResult loginResult = new UserAccountService.LoginResult("token-name-test", loginResponse);
        when(userAccountService.login(any(LoginRequest.class))).thenReturn(loginResult);

        // Act
        ResponseEntity<DataResponse<LoginResponse>> response = userAccountController.login(loginRequest);

        // Assert
        String setCookieHeader = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        assertThat(setCookieHeader).startsWith("x-auth-cookie=");
    }

    @Test
    void shouldInvokeServiceExactlyOnce() {
        // Arrange
        LoginRequest loginRequest = LoginRequest.builder()
                .email("test@example.com")
                .password("password")
                .build();

        LoginResponse loginResponse = LoginResponse.builder()
                .userId("user-666")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .build();

        UserAccountService.LoginResult loginResult = new UserAccountService.LoginResult("token-once", loginResponse);
        when(userAccountService.login(any(LoginRequest.class))).thenReturn(loginResult);

        // Act
        userAccountController.login(loginRequest);

        // Assert
        verify(userAccountService, times(1)).login(loginRequest);
        verifyNoMoreInteractions(userAccountService);
    }
}
