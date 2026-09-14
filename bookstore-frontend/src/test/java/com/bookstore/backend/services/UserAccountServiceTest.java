package com.bookstore.backend.services;

import com.bookstore.backend.dtos.LoginRequest;
import com.bookstore.backend.dtos.LoginResponse;
import com.bookstore.backend.dtos.UserAccount;
import com.bookstore.backend.entities.UserAccountEntity;
import com.bookstore.backend.enums.UserAccountError;
import com.bookstore.backend.exceptions.UserAccountException;
import com.bookstore.backend.mappers.UserAccountMapper;
import com.bookstore.backend.repositories.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserAccountServiceTest {

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UserAccountService userAccountService;

    private final UserAccountMapper mapper = UserAccountMapper.INSTANCE;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Test
    void shouldCreateUserAccountSuccessfully() {
        UserAccount userAccount = UserAccount.builder()
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .password("password123")
                .build();

        UserAccountEntity savedEntity = UserAccountEntity.builder()
                .userId("generated-uuid")
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .password("password123")
                .blocked(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(userAccountRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(userAccountRepository.saveUserAccount(any(UserAccountEntity.class))).thenReturn(savedEntity);

        UserAccount result = userAccountService.createUserAccount(userAccount);

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo("generated-uuid");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getBlocked()).isFalse();

        verify(userAccountRepository).findByEmail("test@example.com");
        verify(userAccountRepository).saveUserAccount(any(UserAccountEntity.class));
    }

    @Test
    void shouldThrowExceptionWhenEmailIsNull() {
        UserAccount userAccount = UserAccount.builder()
                .email(null)
                .firstName("John")
                .password("password123")
                .build();

        assertThatThrownBy(() -> userAccountService.createUserAccount(userAccount))
                .isInstanceOf(UserAccountException.class)
                .hasMessage(UserAccountError.EMAIL_REQUIRED.getMessage());

        verify(userAccountRepository, never()).saveUserAccount(any());
    }

    @Test
    void shouldThrowExceptionWhenEmailIsEmpty() {
        UserAccount userAccount = UserAccount.builder()
                .email("   ")
                .firstName("John")
                .password("password123")
                .build();

        assertThatThrownBy(() -> userAccountService.createUserAccount(userAccount))
                .isInstanceOf(UserAccountException.class)
                .hasMessage(UserAccountError.EMAIL_REQUIRED.getMessage());

        verify(userAccountRepository, never()).saveUserAccount(any());
    }

    @Test
    void shouldThrowExceptionWhenEmailFormatIsInvalid() {
        UserAccount userAccount = UserAccount.builder()
                .email("invalid-email")
                .firstName("John")
                .password("password123")
                .build();

        assertThatThrownBy(() -> userAccountService.createUserAccount(userAccount))
                .isInstanceOf(UserAccountException.class)
                .hasMessage(UserAccountError.INVALID_EMAIL_FORMAT.getMessage());

        verify(userAccountRepository, never()).saveUserAccount(any());
    }

    @Test
    void shouldThrowExceptionWhenFirstNameIsNull() {
        UserAccount userAccount = UserAccount.builder()
                .email("test@example.com")
                .firstName(null)
                .password("password123")
                .build();

        assertThatThrownBy(() -> userAccountService.createUserAccount(userAccount))
                .isInstanceOf(UserAccountException.class)
                .hasMessage(UserAccountError.FIRST_NAME_REQUIRED.getMessage());

        verify(userAccountRepository, never()).saveUserAccount(any());
    }

    @Test
    void shouldThrowExceptionWhenFirstNameIsEmpty() {
        UserAccount userAccount = UserAccount.builder()
                .email("test@example.com")
                .firstName("   ")
                .password("password123")
                .build();

        assertThatThrownBy(() -> userAccountService.createUserAccount(userAccount))
                .isInstanceOf(UserAccountException.class)
                .hasMessage(UserAccountError.FIRST_NAME_REQUIRED.getMessage());

        verify(userAccountRepository, never()).saveUserAccount(any());
    }

    @Test
    void shouldThrowExceptionWhenPasswordIsNull() {
        UserAccount userAccount = UserAccount.builder()
                .email("test@example.com")
                .firstName("John")
                .password(null)
                .build();

        assertThatThrownBy(() -> userAccountService.createUserAccount(userAccount))
                .isInstanceOf(UserAccountException.class)
                .hasMessage(UserAccountError.PASSWORD_REQUIRED.getMessage());

        verify(userAccountRepository, never()).saveUserAccount(any());
    }

    @Test
    void shouldThrowExceptionWhenPasswordIsEmpty() {
        UserAccount userAccount = UserAccount.builder()
                .email("test@example.com")
                .firstName("John")
                .password("   ")
                .build();

        assertThatThrownBy(() -> userAccountService.createUserAccount(userAccount))
                .isInstanceOf(UserAccountException.class)
                .hasMessage(UserAccountError.PASSWORD_REQUIRED.getMessage());

        verify(userAccountRepository, never()).saveUserAccount(any());
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        UserAccount userAccount = UserAccount.builder()
                .email("existing@example.com")
                .firstName("John")
                .password("password123")
                .build();

        UserAccountEntity existingEntity = UserAccountEntity.builder()
                .userId("existing-id")
                .email("existing@example.com")
                .build();

        when(userAccountRepository.findByEmail("existing@example.com"))
                .thenReturn(Optional.of(existingEntity));

        assertThatThrownBy(() -> userAccountService.createUserAccount(userAccount))
                .isInstanceOf(UserAccountException.class)
                .hasMessage(UserAccountError.EMAIL_ALREADY_EXISTS.getMessage())
                .extracting(ex -> ((UserAccountException) ex).getHttpCode())
                .isEqualTo(409);

        verify(userAccountRepository).findByEmail("existing@example.com");
        verify(userAccountRepository, never()).saveUserAccount(any());
    }

    @Test
    void shouldFindUserByUserId() {
        String userId = "test-user-id";
        UserAccountEntity entity = UserAccountEntity.builder()
                .userId(userId)
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .password("password123")
                .blocked(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(userAccountRepository.findByUserId(userId)).thenReturn(Optional.of(entity));

        UserAccount result = userAccountService.findByUserId(userId);

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getEmail()).isEqualTo("test@example.com");

        verify(userAccountRepository).findByUserId(userId);
    }

    @Test
    void shouldThrowExceptionWhenUserIdNotFound() {
        String userId = "non-existent-id";

        when(userAccountRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userAccountService.findByUserId(userId))
                .isInstanceOf(UserAccountException.class)
                .hasMessage(UserAccountError.USER_NOT_FOUND.getMessage());

        verify(userAccountRepository).findByUserId(userId);
    }

    @Test
    void shouldFindUserByEmail() {
        String email = "test@example.com";
        UserAccountEntity entity = UserAccountEntity.builder()
                .userId("test-user-id")
                .email(email)
                .firstName("John")
                .lastName("Doe")
                .password("password123")
                .blocked(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(userAccountRepository.findByEmail(email)).thenReturn(Optional.of(entity));

        UserAccount result = userAccountService.findByUserEmail(email);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(email);
        assertThat(result.getUserId()).isEqualTo("test-user-id");

        verify(userAccountRepository).findByEmail(email);
    }

    @Test
    void shouldThrowExceptionWhenEmailNotFound() {
        String email = "nonexistent@example.com";

        when(userAccountRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userAccountService.findByUserEmail(email))
                .isInstanceOf(UserAccountException.class)
                .hasMessage(UserAccountError.USER_NOT_FOUND.getMessage());

        verify(userAccountRepository).findByEmail(email);
    }

    @Test
    void shouldTestMapperConversion() {
        UserAccountEntity entity = UserAccountEntity.builder()
                .userId("test-id")
                .email("mapper@example.com")
                .firstName("Map")
                .lastName("Struct")
                .password("password")
                .blocked(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        UserAccount dto = mapper.toDto(entity);

        assertThat(dto.getUserId()).isEqualTo(entity.getUserId());
        assertThat(dto.getEmail()).isEqualTo(entity.getEmail());
        assertThat(dto.getFirstName()).isEqualTo(entity.getFirstName());
        assertThat(dto.getLastName()).isEqualTo(entity.getLastName());
        assertThat(dto.getPassword()).isEqualTo(entity.getPassword());
        assertThat(dto.getBlocked()).isEqualTo(entity.getBlocked());

        UserAccountEntity convertedEntity = mapper.toEntity(dto);

        assertThat(convertedEntity.getUserId()).isEqualTo(dto.getUserId());
        assertThat(convertedEntity.getEmail()).isEqualTo(dto.getEmail());
        assertThat(convertedEntity.getFirstName()).isEqualTo(dto.getFirstName());
        assertThat(convertedEntity.getLastName()).isEqualTo(dto.getLastName());
        assertThat(convertedEntity.getPassword()).isEqualTo(dto.getPassword());
        assertThat(convertedEntity.getBlocked()).isEqualTo(dto.getBlocked());
    }

    @Test
    void shouldEncodePasswordWithBCrypt() {
        String rawPassword = "plainPassword123";
        UserAccount userAccount = UserAccount.builder()
                .email("bcrypt@example.com")
                .firstName("BCrypt")
                .lastName("Test")
                .password(rawPassword)
                .build();

        UserAccountEntity savedEntity = UserAccountEntity.builder()
                .userId("bcrypt-uuid")
                .email("bcrypt@example.com")
                .firstName("BCrypt")
                .lastName("Test")
                .password("$2a$10$encodedPasswordHash")
                .blocked(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(userAccountRepository.findByEmail("bcrypt@example.com")).thenReturn(Optional.empty());
        when(userAccountRepository.saveUserAccount(any(UserAccountEntity.class))).thenReturn(savedEntity);

        UserAccount result = userAccountService.createUserAccount(userAccount);

        verify(userAccountRepository).saveUserAccount(any(UserAccountEntity.class));

        assertThat(result.getPassword()).isNotNull();
        assertThat(result.getPassword()).isNotEqualTo(rawPassword);
        assertThat(result.getPassword()).startsWith("$2a$");
    }

    @Test
    void shouldVerifyBCryptPassword() {
        String rawPassword = "mySecretPassword";
        // Generate BCrypt hash dynamically for testing
        String encodedPassword = passwordEncoder.encode(rawPassword);

        boolean matches = userAccountService.verifyPassword(rawPassword, encodedPassword);

        assertThat(matches).isTrue();
    }

    @Test
    void shouldNotMatchIncorrectPassword() {
        String correctPassword = "mySecretPassword";
        String wrongPassword = "wrongPassword";
        // Generate BCrypt hash for the correct password
        String encodedPassword = passwordEncoder.encode(correctPassword);

        boolean matches = userAccountService.verifyPassword(wrongPassword, encodedPassword);

        assertThat(matches).isFalse();
    }

    @Test
    void shouldLoginSuccessfully() {
        String rawPassword = "password123";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        LoginRequest loginRequest = LoginRequest.builder()
                .email("test@example.com")
                .password(rawPassword)
                .build();

        UserAccountEntity userAccountEntity = UserAccountEntity.builder()
                .userId("user-123")
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .password(encodedPassword)
                .blocked(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(userAccountRepository.findByEmail("test@example.com")).thenReturn(Optional.of(userAccountEntity));
        when(jwtService.generateToken("user-123", "test@example.com", "John", "Doe"))
                .thenReturn("jwt-token-mock");

        LoginResponse response = userAccountService.login(loginRequest);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("jwt-token-mock");
        assertThat(response.getUserId()).isEqualTo("user-123");
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getFirstName()).isEqualTo("John");
        assertThat(response.getLastName()).isEqualTo("Doe");

        verify(userAccountRepository).findByEmail("test@example.com");
        verify(jwtService).generateToken("user-123", "test@example.com", "John", "Doe");
    }

    @Test
    void shouldThrowExceptionWhenLoginEmailIsNull() {
        LoginRequest loginRequest = LoginRequest.builder()
                .email(null)
                .password("password123")
                .build();

        assertThatThrownBy(() -> userAccountService.login(loginRequest))
                .isInstanceOf(UserAccountException.class)
                .hasMessage(UserAccountError.EMAIL_REQUIRED.getMessage());

        verify(userAccountRepository, never()).findByEmail(any());
    }

    @Test
    void shouldThrowExceptionWhenLoginEmailIsEmpty() {
        LoginRequest loginRequest = LoginRequest.builder()
                .email("   ")
                .password("password123")
                .build();

        assertThatThrownBy(() -> userAccountService.login(loginRequest))
                .isInstanceOf(UserAccountException.class)
                .hasMessage(UserAccountError.EMAIL_REQUIRED.getMessage());

        verify(userAccountRepository, never()).findByEmail(any());
    }

    @Test
    void shouldThrowExceptionWhenLoginEmailFormatIsInvalid() {
        LoginRequest loginRequest = LoginRequest.builder()
                .email("invalid-email")
                .password("password123")
                .build();

        assertThatThrownBy(() -> userAccountService.login(loginRequest))
                .isInstanceOf(UserAccountException.class)
                .hasMessage(UserAccountError.INVALID_EMAIL_FORMAT.getMessage());

        verify(userAccountRepository, never()).findByEmail(any());
    }

    @Test
    void shouldThrowExceptionWhenLoginPasswordIsNull() {
        LoginRequest loginRequest = LoginRequest.builder()
                .email("test@example.com")
                .password(null)
                .build();

        assertThatThrownBy(() -> userAccountService.login(loginRequest))
                .isInstanceOf(UserAccountException.class)
                .hasMessage(UserAccountError.PASSWORD_REQUIRED.getMessage());

        verify(userAccountRepository, never()).findByEmail(any());
    }

    @Test
    void shouldThrowExceptionWhenLoginPasswordIsEmpty() {
        LoginRequest loginRequest = LoginRequest.builder()
                .email("test@example.com")
                .password("   ")
                .build();

        assertThatThrownBy(() -> userAccountService.login(loginRequest))
                .isInstanceOf(UserAccountException.class)
                .hasMessage(UserAccountError.PASSWORD_REQUIRED.getMessage());

        verify(userAccountRepository, never()).findByEmail(any());
    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundDuringLogin() {
        LoginRequest loginRequest = LoginRequest.builder()
                .email("notfound@example.com")
                .password("password123")
                .build();

        when(userAccountRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userAccountService.login(loginRequest))
                .isInstanceOf(UserAccountException.class)
                .hasMessageContaining("User account not found with email: notfound@example.com")
                .extracting(ex -> ((UserAccountException) ex).getHttpCode())
                .isEqualTo(404);

        verify(userAccountRepository).findByEmail("notfound@example.com");
    }

    @Test
    void shouldThrowExceptionWhenPasswordDoesNotMatch() {
        String correctPassword = "correctPassword123";
        String wrongPassword = "wrongPassword123";
        String encodedPassword = passwordEncoder.encode(correctPassword);

        LoginRequest loginRequest = LoginRequest.builder()
                .email("test@example.com")
                .password(wrongPassword)
                .build();

        UserAccountEntity userAccountEntity = UserAccountEntity.builder()
                .userId("user-123")
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .password(encodedPassword)
                .blocked(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(userAccountRepository.findByEmail("test@example.com")).thenReturn(Optional.of(userAccountEntity));

        assertThatThrownBy(() -> userAccountService.login(loginRequest))
                .isInstanceOf(UserAccountException.class)
                .hasMessage(UserAccountError.LOGIN_FAILED.getMessage())
                .extracting(ex -> ((UserAccountException) ex).getHttpCode())
                .isEqualTo(401);

        verify(userAccountRepository).findByEmail("test@example.com");
    }
}
